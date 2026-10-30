package com.cornmell.superjump.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import javax.swing.DefaultCellEditor
import javax.swing.event.DocumentEvent
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel

class SuperJumpConfigurable : Configurable {
    private var tableModel: JumperTableModel? = null
    private var table: JBTable? = null
    private var fallbackCheckBox: JBCheckBox? = null

    override fun getDisplayName(): String = "SuperJump"

    override fun createComponent(): JComponent {
        val model = JumperTableModel()

        val jumperTable =
            JBTable(model).apply {
                rowHeight = 28

                // Commit in-progress edits when focus leaves the table.
                putClientProperty("terminateEditOnFocusLost", true)

                selectionModel.selectionMode =
                    ListSelectionModel.SINGLE_SELECTION

                columnModel.getColumn(0).apply {
                    preferredWidth = 60
                    maxWidth = 60
                }

                columnModel.getColumn(1).preferredWidth = 160
                columnModel.getColumn(2).preferredWidth = 350
                columnModel.getColumn(3).preferredWidth = 350
                columnModel.getColumn(4).preferredWidth = 250

                // Push text edits into the model on every keystroke so the dialog notices the change (Apply button).
                for (column in 1..4) {
                    val field = JBTextField()
                    field.document.addDocumentListener(
                        object : DocumentAdapter() {
                            override fun textChanged(e: DocumentEvent) {
                                val row = editingRow
                                if (isEditing && editingColumn == column && row in 0 until model.rowCount) {
                                    model.setValueAt(field.text, row, column)
                                }
                            }
                        },
                    )
                    columnModel.getColumn(column).cellEditor = DefaultCellEditor(field)
                }
            }

        tableModel = model
        table = jumperTable

        fun stopEditing() {
            if (jumperTable.isEditing) {
                jumperTable.cellEditor.stopCellEditing()
            }
        }

        fun select(row: Int) {
            if (row in 0 until model.rowCount) {
                jumperTable.setRowSelectionInterval(row, row)
            }
        }

        val decorated =
            ToolbarDecorator
                .createDecorator(jumperTable)
                .setAddAction {
                    stopEditing()
                    model.addRow(JumperState(enabled = true, name = "New Jumper", regex = "", url = ""))
                    select(model.rowCount - 1)
                }.setRemoveAction {
                    stopEditing()
                    val selected = jumperTable.selectedRow

                    if (selected >= 0) {
                        model.removeRow(selected)
                        select(selected.coerceAtMost(model.rowCount - 1))
                    }
                }.setMoveUpAction {
                    stopEditing()
                    val selected = jumperTable.selectedRow

                    if (selected > 0) {
                        model.moveRow(selected, selected - 1)
                        select(selected - 1)
                    }
                }.setMoveDownAction {
                    stopEditing()
                    val selected = jumperTable.selectedRow

                    if (selected >= 0 && selected < model.rowCount - 1) {
                        model.moveRow(selected, selected + 1)
                        select(selected + 1)
                    }
                }.addExtraAction(
                    object : DumbAwareAction("Restore Defaults", "Replace all jumpers with the defaults", AllIcons.Actions.Rollback) {
                        override fun actionPerformed(e: AnActionEvent) {
                            val result =
                                Messages.showYesNoDialog(
                                    "Replace all jumpers with the default configuration?",
                                    "Restore Defaults",
                                    null,
                                )

                            if (result == Messages.YES) {
                                table?.cellEditor?.cancelCellEditing()
                                model.setJumpers(SuperJumpSettings.defaultJumpers())
                            }
                        }
                    },
                ).createPanel()

        return panel {
            row {
                fallbackCheckBox = checkBox("Fall back to jumping to Git when no regular expression matches").component
            }

            row {
                label("Jumpers")
            }

            row {
                comment(
                    "The first enabled jumper whose Regex matches the selection, word or line wins. " +
                        $$"Use $1, $2, ... in the URL for regex capture groups. " +
                        "File Regex (optional) limits a jumper to matching file names or Markdown code block languages.",
                )
            }

            row {
                cell(decorated)
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()
        }
    }

    override fun isModified(): Boolean {
        val model = tableModel ?: return false
        val settings = SuperJumpSettings.getInstance()

        return model.jumpers != settings.jumpers ||
            fallbackCheckBox?.isSelected != settings.fallbackToGit
    }

    override fun apply() {
        val model = tableModel ?: return

        // Commit a checkbox/text edit before validating and reading the model.
        if (table?.isEditing == true) {
            table?.cellEditor?.stopCellEditing()
        }

        validateRows(model)

        SuperJumpSettings
            .getInstance()
            .updateJumpers(model.jumpers)

        fallbackCheckBox?.let { SuperJumpSettings.getInstance().updateFallbackToGit(it.isSelected) }
    }

    override fun reset() {
        table?.cellEditor?.cancelCellEditing()

        tableModel?.setJumpers(
            SuperJumpSettings
                .getInstance()
                .jumpers,
        )

        fallbackCheckBox?.isSelected = SuperJumpSettings.getInstance().fallbackToGit
    }

    override fun disposeUIResources() {
        tableModel = null
        table = null
        fallbackCheckBox = null
    }

    private fun validateRows(model: JumperTableModel) {
        model.jumpers.forEachIndexed { index, jumper ->
            try {
                Regex(jumper.regex)
            } catch (e: IllegalArgumentException) {
                throw ConfigurationException(
                    "Invalid regex in row ${index + 1}: ${e.message}",
                )
            }

            if (jumper.name.isBlank()) {
                throw ConfigurationException(
                    "Jumper name cannot be empty in row ${index + 1}",
                )
            }

            if (jumper.url.isBlank()) {
                throw ConfigurationException(
                    "URL cannot be empty in row ${index + 1}",
                )
            }

            val fileRegex = jumper.fileRegex

            if (!fileRegex.isNullOrBlank()) {
                try {
                    Regex(fileRegex)
                } catch (e: IllegalArgumentException) {
                    throw ConfigurationException(
                        "Invalid file regex in row ${index + 1}: ${e.message}",
                    )
                }
            }
        }
    }

    private class JumperTableModel : AbstractTableModel() {
        private val columns =
            arrayOf(
                "Enabled",
                "Name",
                "Regex",
                "URL",
                "File Regex",
            )

        var jumpers: MutableList<JumperState> =
            SuperJumpSettings
                .getInstance()
                .jumpers
                .toMutableList()
            private set

        override fun getRowCount(): Int = jumpers.size

        override fun getColumnCount(): Int = columns.size

        override fun getColumnName(column: Int): String = columns[column]

        override fun getValueAt(
            rowIndex: Int,
            columnIndex: Int,
        ): Any =
            when (columnIndex) {
                0 -> jumpers[rowIndex].enabled
                1 -> jumpers[rowIndex].name
                2 -> jumpers[rowIndex].regex
                3 -> jumpers[rowIndex].url
                4 -> jumpers[rowIndex].fileRegex ?: ""
                else -> ""
            }

        override fun isCellEditable(
            rowIndex: Int,
            columnIndex: Int,
        ): Boolean = true

        override fun setValueAt(
            value: Any?,
            rowIndex: Int,
            columnIndex: Int,
        ) {
            val current = jumpers[rowIndex]

            jumpers[rowIndex] =
                when (columnIndex) {
                    0 -> {
                        current.copy(
                            enabled = value as Boolean,
                        )
                    }

                    1 -> {
                        current.copy(
                            name = value?.toString() ?: "",
                        )
                    }

                    2 -> {
                        current.copy(
                            regex = value?.toString() ?: "",
                        )
                    }

                    3 -> {
                        current.copy(
                            url = value?.toString() ?: "",
                        )
                    }

                    4 -> {
                        current.copy(
                            fileRegex =
                                value
                                    ?.toString()
                                    ?.takeIf { it.isNotBlank() },
                        )
                    }

                    else -> {
                        current
                    }
                }

            fireTableCellUpdated(rowIndex, columnIndex)
        }

        override fun getColumnClass(column: Int): Class<*> =
            if (column == 0) {
                Boolean::class.java
            } else {
                String::class.java
            }

        fun addRow(jumper: JumperState) {
            jumpers.add(jumper)

            fireTableRowsInserted(
                jumpers.lastIndex,
                jumpers.lastIndex,
            )
        }

        fun removeRow(index: Int) {
            jumpers.removeAt(index)

            fireTableRowsDeleted(index, index)
        }

        fun setJumpers(newJumpers: List<JumperState>) {
            jumpers = newJumpers.toMutableList()
            fireTableDataChanged()
        }

        fun moveRow(
            fromIndex: Int,
            toIndex: Int,
        ) {
            if (fromIndex !in jumpers.indices ||
                toIndex !in jumpers.indices ||
                fromIndex == toIndex
            ) {
                return
            }

            val jumper = jumpers.removeAt(fromIndex)
            jumpers.add(toIndex, jumper)

            fireTableRowsUpdated(
                minOf(fromIndex, toIndex),
                maxOf(fromIndex, toIndex),
            )
        }
    }
}
