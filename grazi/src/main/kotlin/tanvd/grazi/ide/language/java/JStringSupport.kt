package tanvd.grazi.ide.language.java

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.tree.IElementType
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class JStringSupport : LanguageSupport {
    companion object {
        private val disabledRules = setOf("UPPERCASE_SENTENCE_START", "PUNCTUATION_PARAGRAPH_END")
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val literals = file.filterFor<PsiLiteralExpressionImpl>()

        for (str in literals.filterForType(JavaTokenType.STRING_LITERAL)) {
            addAll(SanitizingGrammarChecker.default.check(str) { it.innerText ?: "" }
                    .filter { it.info.rule.id !in disabledRules })
            ProgressManager.checkCanceled()
        }

        for (str in literals.filterForType(JavaTokenType.RAW_STRING_LITERAL)) {
            addAll(SanitizingGrammarChecker.default.check(str) { it.rawString ?: "" }
                    .filter { it.info.rule.id !in disabledRules })

            ProgressManager.checkCanceled()
        }
    }

    private fun Collection<PsiLiteralExpressionImpl>.filterForType(type: IElementType) = filter { it.literalElementType == type }
}