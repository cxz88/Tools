package com.chenxinzhi.plugins.intellij.action

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.HandlerService
import com.intellij.codeInsight.daemon.JavaErrorBundle
import com.intellij.core.JavaPsiBundle
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.JavaCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.JavaTemplateUtil
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.PackageChooserDialog
import com.intellij.java.JavaBundle
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.ui.IconManager
import com.intellij.ui.PlatformIcons
import com.intellij.util.IncorrectOperationException


class CreateClassAction : JavaCreateTemplateInPackageAction<PsiClass?>(
    "",
    JavaBundle.message("action.create.new.class.description"),
    IconManager.getInstance().getPlatformIcon(PlatformIcons.Enum),
    true
), DumbAware {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(JavaBundle.message("action.create.new.class"))
        val level = PsiUtil.getLanguageLevel(directory)
        builder.addKind(
            JavaPsiBundle.message("node.enum.tooltip"),
            com.intellij.util.PlatformIcons.ENUM_ICON,
            JavaTemplateUtil.INTERNAL_ENUM_TEMPLATE_NAME
        )
        val dirs = arrayOf<PsiDirectory?>(directory)
        for (template in FileTemplateManager.getInstance(project).allTemplates) {
            val handler = FileTemplateUtil.findHandler(template)
            if (handler is JavaCreateFromTemplateHandler && handler.handlesTemplate(template) && handler.canCreate(dirs)) {
                builder.addKind(template.name, JavaFileType.INSTANCE.icon, template.name)
            }
        }
        builder.setValidator(object : InputValidatorEx {
            override fun getErrorText(inputString: String): String? {
                if (!inputString.isEmpty() && !PsiNameHelper.getInstance(project).isQualifiedName(inputString)) {
                    return JavaErrorBundle.message("create.class.action.this.not.valid.java.qualified.name")
                }
                val shortName = StringUtil.getShortName(inputString)
                if (PsiTypesUtil.isRestrictedIdentifier(shortName, level)) {
                    return JavaErrorBundle.message("restricted.identifier", shortName)
                }
                return null
            }

            override fun checkInput(inputString: String?): Boolean {
                return true
            }

            override fun canClose(inputString: String?): Boolean {
                return !StringUtil.isEmptyOrSpaces(inputString) && getErrorText(inputString!!) == null
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val project = e.getData(CommonDataKeys.PROJECT)
        if (editor != null && project != null) {
            val selectionModel = editor.selectionModel
            val selectedText = selectionModel.selectedText
            if (!selectedText.isNullOrBlank()) {
                e.presentation.isEnabledAndVisible = parseSingleLine(selectedText) != null
            } else {
                e.presentation.isEnabledAndVisible = false
            }
        }
    }

    override fun removeExtension(templateName: String?, className: String): String {
        return StringUtil.trimEnd(className, ".java")
    }

    override fun getErrorTitle(): String {
        return JavaBundle.message("title.cannot.create.class")
    }


    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String?): String {
        val psiPackage = JavaDirectoryService.getInstance().getPackage(directory)
        return JavaBundle.message(
            "progress.creating.class", StringUtil.getQualifiedName(psiPackage?.qualifiedName ?: "", newName)
        )
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    @Throws(IncorrectOperationException::class)
    override fun doCreate(dir: PsiDirectory, className: String, templateName: String): PsiClass? {
        val project = dir.project
        //弹出包选择对话框
        val dialog = PackageChooserDialog(LanguageBundle.getLazyMessage("choose.package").get(), project)
        dialog.selectPackage("org.springblade")
        dialog.show()
        return if (dialog.isOK) {
            dialog.selectedPackage?.directories?.firstOrNull()?.let {
                JavaDirectoryService.getInstance().createClass(it, className, templateName, true)
            }
        } else {
            null
        }

    }

    override fun getNavigationElement(createdElement: PsiClass): PsiElement? {
        if (createdElement.isRecord) {
            val header = createdElement.recordHeader
            if (header != null) {
                return header.lastChild
            }
        }
        return createdElement.lBrace
    }

    var p: Pair<String, Map<Int, String>>? = null

    fun parseSingleLine(input: String): Pair<String, Map<Int, String>>? {
        val regex = Regex("""^(.*?)(\d+\..+)$""")
        val match = regex.find(input) ?: return null

        val title = match.groupValues[1]
        val keyValuesText = match.groupValues[2]

        val itemRegex = Regex("""(\d+)\.([^0-9]+)""")
        val result = mutableMapOf<Int, String>()

        itemRegex.findAll(keyValuesText).forEach {
            val key = it.groupValues[1].toInt()
            val value = it.groupValues[2].trim()
            result[key] = value
        }

        return Pair(title.trim(), result).apply {
            p = this
        }
    }

    val units = listOf(
        "ZERO",
        "ONE",
        "TWO",
        "THREE",
        "FOUR",
        "FIVE",
        "SIX",
        "SEVEN",
        "EIGHT",
        "NINE",
        "TEN",
        "ELEVEN",
        "TWELVE",
        "THIRTEEN",
        "FOURTEEN",
        "FIFTEEN",
        "SIXTEEN",
        "SEVENTEEN",
        "EIGHTEEN",
        "NINETEEN"
    )

    val tens = listOf(
        "", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY"
    )

    fun numberToWords(n: Int): String {
        require(n in 0..100) { "Number out of range" }
        return when {
            n < 20 -> units[n]
            n < 100 -> {
                val tenPart = tens[n / 10]
                val unitPart = units[n % 10]
                if (unitPart == "ZERO") tenPart else "${tenPart}_${unitPart}"
            }

            else -> "ONE_HUNDRED"
        }
    }

    override fun postProcess(
        createdElement: PsiClass, templateName: String?, customProperties: MutableMap<String?, String?>?
    ) {
        val project = createdElement.project
        val service = project.service<HandlerService>()
        service.handler {
            runWriteCommandAction(project) {
                val factory = JavaPsiFacade.getElementFactory(project)
                val manager = PsiManager.getInstance(project)
                val resolveScope = GlobalSearchScope.allScope(project)
                super.postProcess(createdElement, templateName, customProperties)
                val fieldKey = factory.createField("key", PsiTypes.intType())
                fieldKey.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
                fieldKey.modifierList?.setModifierProperty(PsiModifier.FINAL, true)
                val fieldValue = factory.createField("value", PsiType.getJavaLangString(manager, resolveScope))
                fieldValue.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
                fieldValue.modifierList?.setModifierProperty(PsiModifier.FINAL, true)
                val a1 = try {
                    factory.createAnnotationFromText("@lombok.AllArgsConstructor", null)
                } catch (_: Exception) {
                    Messages.showWarningDialog(
                        project,
                        LanguageBundle.getLazyMessage("notfound.class.content", "lombok.AllArgsConstructor").get(),
                        LanguageBundle.getLazyMessage("notfound.class.title").get()
                    )
                    return@runWriteCommandAction
                }
                val a2 = try {
                    factory.createAnnotationFromText("@lombok.Getter", null)
                } catch (_: Exception) {
                    Messages.showWarningDialog(
                        project,
                        LanguageBundle.getLazyMessage("notfound.class.content", "lombok.Getter").get(),
                        LanguageBundle.getLazyMessage("notfound.class.title").get()
                    )
                    return@runWriteCommandAction
                }
                val docComment = factory.createCommentFromText(
                    """// ${p?.first ?: ""}""".trimIndent(), null
                )
                val enums = p?.let { v ->
                    val second = v.second
                    second.map { i ->
                        factory.createEnumConstantFromText(
                            """${numberToWords(i.key)}(${i.key},"${i.value}")""", null
                        )

                    }
                }

                createdElement.apply {
                    add(fieldKey)
                    add(fieldValue)
                    createdElement.modifierList?.apply {
                        addBefore(a1, createdElement.modifierList?.firstChild)
                        addBefore(a2, createdElement.modifierList?.firstChild)
                        addBefore(docComment, createdElement.modifierList?.firstChild)
                    }
                    enums?.forEach(::add)
                    JavaCodeStyleManager.getInstance(project).apply {
                        shortenClassReferences(createdElement)
                        invokeLater {
                            runWriteCommandAction(project) {
                                CodeStyleManager.getInstance(project).apply {
                                    reformat(createdElement, true)
                                }
                            }

                        }
                    }
                }

            }

        }

    }
}
