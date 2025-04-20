package org.plugin.mvcUtil;

import com.ibm.icu.text.CaseMap;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.Project;

import java.util.List;

public class Button extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        // Abre el cuadro de diálogo para agregar o quitar clases
        ClassDialog dialog = new ClassDialog();
        dialog.show();

        if (dialog.isOK()) {
            // Si el usuario hace clic en OK, obtiene la lista de clases
            List<String> classList = dialog.getClasses();

            for (int i = 0; i < classList.size(); i++) {
                String clazz = classList.get(i);
                // Transformar la primera letra a mayúscula sin comprobar si es minúscula
                classList.set(i, clazz.substring(0, 1).toUpperCase() + clazz.substring(1));
            }


            // Mostrar las clases agregadas en un cuadro de mensaje
            StringBuilder classesMessage = new StringBuilder("Clases Agregadas:\n");
            for (String className : classList) {
                classesMessage.append(className).append("\n");
            }
            Messages.showMessageDialog(project, classesMessage.toString(), "Clases", Messages.getInformationIcon());

            boolean usarSingleton = dialog.isSingletonEnabled();
            boolean usarLombok = dialog.isLombokEnabled();

            FileManager.createMvcPackages(project);
            FileManager.createMvcClasses(project,classList,usarSingleton,usarLombok);
        }
    }
}
