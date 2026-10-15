package com.cornmell.superjump.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import java.awt.Component
import javax.swing.DefaultCellEditor
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class SuperJumpConfigurable : Configurable {
    private var tableModel: JumperTableModel? = null
    private var table: JBTable? = null

    override fun getDisplayName(): String = "SuperJump"

    override fun createComponent(): JComponent {
        val model = JumperTableModel()

        val jumperTable =
            JBTable(model).apply {
                rowHeight = 28

                selectionModel.selectionMode =
                    ListSelectionModel.SINGLE_SELECTION

                columnModel.getColumn(0).apply {
                    preferredWidth = 60
                    maxWidth = 60

                    cellRenderer = CheckBoxRenderer()
                    cellEditor = DefaultCellEditor(JCheckBox())
                }

                columnModel.getColumn(1).preferredWidth = 160
                columnModel.getColumn(2).preferredWidth = 350
                columnModel.getColumn(3).preferredWidth = 350
                columnModel.getColumn(4).preferredWidth = 250
            }

        tableModel = model
        table = jumperTable

        return panel {
            row {
                label("Jumpers")
            }

            row {
                scrollCell(jumperTable)
                    .align(Align.FILL)
                    .resizableColumn()
            }.resizableRow()

            row {
                button("Add") {
                    val row = model.rowCount

                    model.addRow(
                        JumperState(
                            enabled = true,
                            name = "New Jumper",
                            regex = "",
                            url = "",
                        ),
                    )

                    jumperTable.setRowSelectionInterval(row, row)
                }

                button("Remove") {
                    val selected = jumperTable.selectedRow

                    if (selected >= 0) {
                        model.removeRow(selected)

                        if (model.rowCount > 0) {
                            val row = selected.coerceAtMost(model.rowCount - 1)
                            jumperTable.setRowSelectionInterval(row, row)
                        }
                    }
                }

                button("Move Up") {
                    val selected = jumperTable.selectedRow

                    if (selected > 0) {
                        if (jumperTable.isEditing) {
                            jumperTable.cellEditor.stopCellEditing()
                        }

                        model.moveRow(selected, selected - 1)
                        jumperTable.setRowSelectionInterval(selected - 1, selected - 1)
                    }
                }

                button("Move Down") {
                    val selected = jumperTable.selectedRow

                    if (selected >= 0 && selected < model.rowCount - 1) {
                        if (jumperTable.isEditing) {
                            jumperTable.cellEditor.stopCellEditing()
                        }

                        model.moveRow(selected, selected + 1)
                        jumperTable.setRowSelectionInterval(selected + 1, selected + 1)
                    }
                }

                button("Defaults") {
                    val result = com.intellij.openapi.ui.Messages.showYesNoDialog(
                        "Replace all jumpers with the default configuration?",
                        "Restore Defaults",
                        null,
                    )

                    if (result == com.intellij.openapi.ui.Messages.YES) {
                        table?.cellEditor?.cancelCellEditing()
                        model.setJumpers(SuperJumpSettings.defaultJumpers())
                    }
                }
            }
        }
    }

    override fun isModified(): Boolean {
        val model = tableModel ?: return false

        return model.jumpers != SuperJumpSettings.getInstance().jumpers
    }

    override fun apply() {
        val model = tableModel ?: return

        validateRows(model)

        // Commit a checkbox/text edit before reading the model.
        if (table?.isEditing == true) {
            table?.cellEditor?.stopCellEditing()
        }

        SuperJumpSettings
            .getInstance()
            .updateJumpers(model.jumpers)
    }

    override fun reset() {
        table?.cellEditor?.cancelCellEditing()

        tableModel?.setJumpers(
            SuperJumpSettings
                .getInstance()
                .jumpers,
        )
    }

    override fun disposeUIResources() {
        tableModel = null
        table = null
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

            if (!jumper.fileRegex.isNullOrBlank()) {
                try {
                    Regex(jumper.fileRegex.toString())
                } catch (e: IllegalArgumentException) {
                    throw ConfigurationException(
                        "Invalid file regex in row ${index + 1}: ${e.message}",
                    )
                }
            }
        }
    }

    private class CheckBoxRenderer : DefaultTableCellRenderer() {
        private val checkBox = JCheckBox()

        init {
            checkBox.horizontalAlignment = CENTER
        }

        override fun getTableCellRendererComponent(
            table: javax.swing.JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            checkBox.isSelected = value == true

            checkBox.background =
                if (isSelected) {
                    table.selectionBackground
                } else {
                    table.background
                }

            return checkBox
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
