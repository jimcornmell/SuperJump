package com.cornmell.superjump

import com.cornmell.superjump.settings.SuperJumpSettings
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import git4idea.branch.GitBranchUtil
import git4idea.repo.GitRepositoryManager
import java.util.concurrent.CancellationException

private const val NOTIFICATION_GROUP = "SuperJump"

class SuperJumpAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        protectCallback(e.project, "Unable to perform SuperJump action") {
            performAction(e)
        }
    }

    private fun protectCallback(project: Project?, message: String, callback: () -> Unit) {
        try {
            callback()
        } catch (exception: ProcessCanceledException) {
            throw exception
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            thisLogger().warn(message, exception)
            notify(project, "$message. See the IDE log for details.", NotificationType.ERROR)
        }
    }

    private fun performAction(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project
        val document = editor.document
        val virtualFile = FileDocumentManager.getInstance().getFile(document)
        val filename = virtualFile?.name.orEmpty()
        val offset = editor.caretModel.offset
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val columnNumber = offset - lineStart
        val currentLineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, document.getLineEndOffset(lineNumber)))
        val selectedText = editor.selectionModel.selectedText
        val actionId = e.actionManager.getId(this)
        val isSuperJumpGit = actionId == "SuperJumpGit"

        val txtUrl = if (isSuperJumpGit) {
            null
        } else {
            val hasSelection = !selectedText.isNullOrBlank()

            TextToUrl().toUrl(
                filename,
                lineNumber,
                if (hasSelection) -1 else columnNumber,
                if (hasSelection) selectedText else currentLineText,
                document.charsSequence
            )
        }

        if (UrlSupport.isBrowsable(txtUrl)) {
            openUrl(actionId, txtUrl!!)
            return
        }

        if (!isSuperJumpGit && !SuperJumpSettings.getInstance().fallbackToGit) {
            showError(project, false)
            return
        }

        if (project == null || virtualFile == null) {
            showError(project, isSuperJumpGit)
            return
        }

        resolveGitUrl(project, virtualFile, lineNumber, actionId, isSuperJumpGit)
    }

    private fun resolveGitUrl(
        project: Project,
        virtualFile: VirtualFile,
        lineNumber: Int,
        actionId: String?,
        isSuperJumpGit: Boolean
    ) {
        ReadAction
            .nonBlocking<String> { getGitUrl(project, virtualFile, lineNumber) }
            .expireWith(project)
            .finishOnUiThread(ModalityState.any()) { gitUrl ->
                protectCallback(project, "Unable to open Git URL") {
                    if (UrlSupport.isBrowsable(gitUrl)) {
                        openUrl(actionId, gitUrl)
                    } else {
                        showError(project, isSuperJumpGit)
                    }
                }
            }
            .submit(AppExecutorUtil.getAppExecutorService())
            .onError { error ->
                thisLogger().warn("Unable to resolve Git URL", error)
                notify(project, "Unable to resolve the Git URL. See the IDE log for details.", NotificationType.ERROR)
            }
    }

    private fun getGitUrl(
        project: Project,
        virtualFile: VirtualFile,
        lineNumber: Int
    ): String {
        val repository = GitRepositoryManager.getInstance(project)
            .getRepositoryForFile(virtualFile)
            ?: return ""

        // Prefer the remote the current branch tracks, then "origin", then any remote.
        val trackedRemote = repository.currentBranch
            ?.let { GitBranchUtil.getTrackInfoForBranch(repository, it) }
            ?.remote
        val remote = trackedRemote
            ?: repository.remotes.firstOrNull { it.name == "origin" }
            ?: repository.remotes.firstOrNull()
        val remoteUrl = remote?.urls?.firstOrNull() ?: return ""

        val branchName = repository.currentBranchName
        val revision = branchName ?: repository.currentRevision ?: return ""
        val filePath = VfsUtilCore.getRelativePath(virtualFile, repository.root, '/') ?: return ""

        return GitFileUrl.build(remoteUrl, revision, filePath, lineNumber, branchName == null)
    }

    private fun openUrl(actionId: String?, url: String) {
        thisLogger().info("$actionId: Opening URL: $url")
        BrowserUtil.browse(url)
    }

    private fun showError(project: Project?, isSuperJumpGit: Boolean) {
        if (isSuperJumpGit) {
            notify(
                project,
                "No Git URL is available. Check that the file belongs to a Git repository with a remote and a current branch or commit.",
                NotificationType.WARNING
            )
        } else {
            notify(project, "No valid URL could be generated for the selection.", NotificationType.WARNING)
        }
    }

    private fun notify(project: Project?, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification("SuperJump", message, type)
            .notify(project)
    }
}
