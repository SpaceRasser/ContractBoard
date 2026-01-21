package com.contractboard.contracts;

import com.contractboard.config.ConfigService;
import com.contractboard.config.TemplateLoader;
import com.contractboard.objectives.ObjectiveData;
import com.contractboard.objectives.ObjectiveDataJson;
import com.contractboard.objectives.ObjectiveType;
import com.contractboard.util.TimeUtil;
import com.contractboard.util.WeightedRandom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ContractGenerator {
    private final TemplateLoader templateLoader;
    private final ConfigService configService;

    public ContractGenerator(TemplateLoader templateLoader, ConfigService configService) {
        this.templateLoader = templateLoader;
        this.configService = configService;
    }

    public List<ContractInstance> generate(PlayerProfile profile, UUID playerUuid) {
        long epochDay = TimeUtil.currentEpochDay(configService.getDailyResetTime());
        long seed = (playerUuid.toString() + ":" + epochDay).hashCode();
        Random random = new Random(seed);
        List<ContractInstance> results = new ArrayList<>();
        Set<String> usedTemplates = new HashSet<>();
        int perDay = configService.getContractsPerDay();

        List<ContractTemplate> available = templateLoader.getTemplates();
        List<WeightedTemplate> weighted = new ArrayList<>();
        for (ContractTemplate template : available) {
            if (profile.getRep(template.getFaction()) < template.getRequirements().getMinRep()) {
                continue;
            }
            weighted.add(new WeightedTemplate(template));
        }
        if (weighted.isEmpty()) {
            return results;
        }

        while (results.size() < perDay && usedTemplates.size() < weighted.size()) {
            ContractTemplate pick = WeightedRandom.pick(weighted, random).template;
            if (usedTemplates.contains(pick.getId())) {
                continue;
            }
            usedTemplates.add(pick.getId());
            ObjectiveData data = ObjectiveDataJson.fromTemplate(pick.getObjectiveType(), pick.getObjective().getTarget(), pick.getObjective().getTypes());
            results.add(new ContractInstance(
                UUID.randomUUID(),
                playerUuid,
                pick.getId(),
                pick.getFaction(),
                pick.getObjectiveType(),
                data,
                0,
                pick.getObjective().getTarget(),
                ContractStatus.ACTIVE,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            ));
        }
        return results;
    }

    public TemplateLoader getTemplateLoader() {
        return templateLoader;
    }

    private static class WeightedTemplate implements WeightedRandom.Weighted {
        private final ContractTemplate template;

        private WeightedTemplate(ContractTemplate template) {
            this.template = template;
        }

        @Override
        public int weight() {
            return template.getWeight();
        }
    }
}
