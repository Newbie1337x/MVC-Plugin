package org.plugin.mvcUtil;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FileManager {

    private Project project;
    private boolean lombok;
    private boolean singleton;
    private VirtualFile root;

    FileManager(Project project,boolean lombok, boolean singleton){
        this.lombok = lombok;
        this.singleton = singleton;
        this.project = project;
        generateRoot();

    }


    private void generateRoot() {

        root = ProjectUtil.guessProjectDir(project);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VfsUtil.createDirectoryIfMissing(root, "src");
                root = ProjectUtil.guessProjectDir(project).findChild("src");
                VfsUtil.createDirectoryIfMissing(root, "generated");
                root = root.findChild("generated");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void createMvcPackages() {


        // Crear los paquetes para MVC dentro de src
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {

                // Crear el paquete model
                VfsUtil.createDirectoryIfMissing(root, "model");
                VfsUtil.createDirectoryIfMissing(root, "repository");
                VfsUtil.createDirectoryIfMissing(root, "service");
                VfsUtil.createDirectoryIfMissing(root, "controller");
                VfsUtil.createDirectoryIfMissing(root, "view");
                VfsUtil.createDirectoryIfMissing(root, "config");
                // Crear el paquete view

                // Crear el paquete controller

            } catch (IOException e) {
                throw new RuntimeException("No se pudieron crear los paquetes MVC", e);
            }
        });
    }


    // Método principal para crear paquetes y clases
    public void createMvcClass(String packageName, String className) {
        String classFileName = className + ".java";
        VirtualFile packageDir = root.findChild(packageName); // asumimos que ya existe
        VirtualFile[] classFile = new VirtualFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                classFile[0] = packageDir.createChildData(null, classFileName);
            } catch (IOException e) {
                throw new RuntimeException("No se pudo crear el archivo para la clase " + className, e);
            }
        });

        String classContent;
        if (singleton && (packageName.equals("repository") || packageName.equals("service") || packageName.equals("controller"))) {
            classContent = "package " + packageName + ";\n\n" +
                    (lombok ? "import lombok.Getter;\n\n" : "") +
                    "public class " + className + " {\n" +
                    generateSingleton(className, lombok) + "\n}";
        } else {
            classContent = "package " + packageName + ";\n\npublic class " + className + " {\n\n}";
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                classFile[0].setBinaryContent(classContent.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("No se pudo escribir en el archivo para la clase " + className, e);
            }
        });
    }

    // Método para crear varias clases en diferentes paquetes
    public void createMvcClasses(List<String> classNames) {

        for (String className : classNames) {
            createMvcClass("model", className + "Entity");
            createMvcClass("controller", className + "Controller");
            createMvcClass("service", className + "Service");
            createMvcClass("repository", className + "Repository");
        }
    }

    public static String generateSingleton(String className,boolean lombok){

        if(lombok){
            return "\n@Getter\n" +
                    "private static final " + className + " instance = new "+ className + "();";
        }

        return "private static final " + className + " instance = new "+ className + "();";
    }
}

