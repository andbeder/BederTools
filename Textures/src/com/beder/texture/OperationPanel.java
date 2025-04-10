package com.beder.texture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OperationPanel extends JPanel {

    private final Operations.Operation operation;
    private final Runnable onEdit;
    private final Runnable onDelete;

    /**
     * Constructs a panel displaying information about a single Operation.
     *
     * @param operation The Operation to display (and potentially edit/delete).
     * @param onEdit    A callback that is invoked when the user clicks the "Edit" button.
     * @param onDelete  A callback that is invoked when the user clicks the "Delete" button.
     */
    public OperationPanel(Operations.Operation operation,
                          Runnable onEdit,
                          Runnable onDelete) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.operation = operation;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        // A label showing the operation’s short description
        JLabel descLabel = new JLabel(operation.getDescription());

        // Buttons: Edit + Delete
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        // Add them to this panel
        add(descLabel);
        add(editButton);
        add(deleteButton);

        // Wire up the actions:
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onEdit != null) {
                    onEdit.run();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onDelete != null) {
                    onDelete.run();
                }
            }
        });
    }

    /**
     * Optionally, you could expose the underlying operation for more advanced logic.
     */
    public Operations.Operation getOperation() {
        return operation;
    }
}