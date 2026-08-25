package com.begoml.bridge.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

private const val ComposableAnnotation = "Composable"
private val TextArgumentNames = setOf("text", "title", "label", "contentDescription", "placeholder")

/**
 * Flags user-visible strings written straight into a composable.
 *
 * Bridge ships in English only today, but every string lives in `strings.xml` from the first
 * commit, because retrofitting that later means editing every screen. This rule is what keeps the
 * decision true; without it the convention survives exactly as long as everyone remembers it.
 */
class HardcodedComposeString(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        id = "HardcodedComposeString",
        severity = Severity.Maintainability,
        description = "User-visible text belongs in strings.xml, read through Res.string.",
        debt = Debt.FIVE_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.getStrictParentOfType<KtNamedFunction>()?.isComposable() != true) return

        expression.valueArguments.forEach { argument ->
            val literal = argument.getArgumentExpression() as? KtStringTemplateExpression ?: return@forEach
            if (literal.text.trim('"').isBlank()) return@forEach
            if (!argument.namesAUserVisibleSlot(expression)) return@forEach

            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(literal),
                    message = "Move this text into strings.xml and read it with stringResource().",
                ),
            )
        }
    }

    private fun KtNamedFunction.isComposable(): Boolean =
        annotationEntries.any { entry: KtAnnotationEntry ->
            entry.shortName?.asString() == ComposableAnnotation
        }

    private fun org.jetbrains.kotlin.psi.KtValueArgument.namesAUserVisibleSlot(
        call: KtCallExpression,
    ): Boolean {
        val argumentName = getArgumentName()?.asName?.asString()
        if (argumentName != null) return argumentName in TextArgumentNames

        val calleeName = call.calleeExpression?.text ?: return false
        return calleeName == "Text" && call.valueArguments.firstOrNull() === this
    }
}
