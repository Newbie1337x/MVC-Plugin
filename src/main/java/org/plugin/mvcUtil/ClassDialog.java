package org.plugin.mvcUtil;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDialog extends DialogWrapper {

    private JBTextField classNameField;
    private JList<String> classList;
    private DefaultListModel<String> listModel;
    private JCheckBox singletonCheckBox;
    private JCheckBox lombokCheckBox;
    private JPanel panel;

    public ClassDialog() {
        super(true); // Modal
        setTitle("Agregar/Quitar Clase");
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        panel = new JPanel(new BorderLayout());

        // Campo de texto
        JLabel label = new JLabel("Clase:");
        classNameField = new JBTextField();
        classNameField.setPreferredSize(new Dimension(300, 30));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(label);
        inputPanel.add(classNameField);

        // Checkboxes
        singletonCheckBox = new JCheckBox("Habilitar Singleton");
        lombokCheckBox = new JCheckBox("Usar Lombok");

        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 1));
        checkBoxPanel.add(singletonCheckBox);
        checkBoxPanel.add(lombokCheckBox);

        // Lista de clases
        listModel = new DefaultListModel<>();
        classList = new JList<>(listModel);
        classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Botones
        JButton addButton = new JButton("+");
        addButton.addActionListener(e -> addClassToList());

        JButton removeButton = new JButton("-");
        removeButton.addActionListener(e -> removeClassFromList());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        // Panel para opciones y botones
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(checkBoxPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Armado del panel principal
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(classList), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    protected ValidationInfo doValidate() {
        if (listModel.isEmpty()) {
            return new ValidationInfo("Debe agregar al menos una clase.", classNameField);
        }
        return null;
    }

    private void addClassToList() {
        String className = classNameField.getText().trim();

        if (className.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "El nombre de la clase no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (listModel.contains(className)) {
            JOptionPane.showMessageDialog(panel, "La clase ya está en la lista", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        listModel.addElement(className);
        classNameField.setText("");
    }

    private void removeClassFromList() {
        int selectedIndex = classList.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
        }
    }

    public List<String> getClasses() {
        List<String> classes = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            classes.add(listModel.getElementAt(i));
        }
        return classes;
    }

    public boolean isSingletonEnabled() {
        return singletonCheckBox.isSelected();
    }

    public boolean isLombokEnabled() {
        return lombokCheckBox.isSelected();
    }
}
