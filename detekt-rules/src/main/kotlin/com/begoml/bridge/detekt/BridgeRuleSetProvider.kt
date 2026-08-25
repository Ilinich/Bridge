package com.begoml.bridge.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class BridgeRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "bridge"

    override fun instance(config: Config): RuleSet = RuleSet(
        id = ruleSetId,
        rules = listOf(HardcodedComposeString(config)),
    )
}
