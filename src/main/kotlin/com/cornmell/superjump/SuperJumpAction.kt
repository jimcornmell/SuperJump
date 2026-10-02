package com.cornmell.superjump

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import fleet.util.isValidUriString
import git4idea.repo.GitRepositoryManager

class SuperJumpAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (editor == null) {
            Messages.showErrorDialog("No editor found", "Error")
            return
        }

        val document = editor.document
        val offset = editor.caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val columnNumber = editor.caretModel.logicalPosition.column
        val textRange = TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber))
        val currentLineText = document.getText(textRange)
        val actionId = e.actionManager.getId(this)
        val isSuperJumpGit = actionId == "SuperJumpGit"
        val selectedText = editor.selectionModel.selectedText

        val url = if (isSuperJumpGit) {
            getGitUrl(e, lineNumber)
        } else {
            val txtUrl = if (selectedText.isNullOrBlank()) {
                thisLogger().info("$actionId: No text selected, using current line: $currentLineText")
                TextToUrl().toUrl(lineNumber, columnNumber, currentLineText)
            } else {
                thisLogger().info("$actionId: Text selected: $selectedText")
                TextToUrl().toUrl(lineNumber, columnNumber, selectedText)
            }

            if (txtUrl?.isValidUriString() == true && txtUrl.isNotBlank()) {
                txtUrl
            } else {
                getGitUrl(e, lineNumber)
            }
        }

        if (url.isValidUriString() && url.isNotBlank()) {
            thisLogger().info("$actionId: Opening URL: $url")
            BrowserUtil.browse(url)
        } else {
            if (isSuperJumpGit) {
                Messages.showErrorDialog(
                    "$actionId: No valid URL could be generated for the selection, is this a GIT repository?", "Not A Git Repository."
                )
            } else {
                Messages.showErrorDialog("$actionId: No valid URL could be generated for the selection", "No URL")
            }
        }
    }

    private fun getGitUrl(e: AnActionEvent, lineNumber: Int): String {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val repositoryManager = e.project?.let { GitRepositoryManager.getInstance(it) }
        val repository = virtualFile?.let { repositoryManager?.getRepositoryForFile(it) }
        val remoteUrl = repository?.remotes?.asSequence()?.flatMap { it.urls.asSequence() }?.firstOrNull()

        if (remoteUrl == null) {
            return ""
        }

        val repositoryUrl = when {
            remoteUrl.startsWith("git@") -> "https://${remoteUrl.removePrefix("git@").replaceFirst(":", "/")}"
            remoteUrl.startsWith("ssh://") -> "https://${remoteUrl.removePrefix("ssh://").removePrefix("git@")}"
            else -> remoteUrl
        }.removeSuffix(".git")
        val lineUrl = if (repositoryUrl.contains("gitlab", ignoreCase = true)) {
            "/-/blob"
        } else {
            "/blob"
        }
        val branch = repository.currentBranchName ?: "HEAD"
        val filePath = virtualFile.path.removePrefix("${repository.root.path}/")

        return "$repositoryUrl$lineUrl/$branch/$filePath#L${lineNumber + 1}"
    }
}
