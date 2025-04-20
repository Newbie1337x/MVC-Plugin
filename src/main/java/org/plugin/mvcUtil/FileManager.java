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

    public static void createMvcPackages(Project project) {
        // Obtener la raíz del proyecto
        VirtualFile baseDir = ProjectUtil.guessProjectDir(project);

        // Buscar la carpeta src dentro de la raíz del proyecto
        final VirtualFile srcDir = baseDir.findChild("src");
        if (srcDir == null) {
            // Si no existe, se crea la carpeta src
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    VfsUtil.createDirectoryIfMissing(baseDir, "src");
                } catch (IOException e) {
                    throw new RuntimeException("No se pudo crear la carpeta src", e);
                }
            });
        }

        // Crear los paquetes para MVC dentro de src
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {

                // Crear el paquete model
                VfsUtil.createDirectoryIfMissing(srcDir, "model");
                VfsUtil.createDirectoryIfMissing(srcDir, "repository");
                VfsUtil.createDirectoryIfMissing(srcDir, "service");
                VfsUtil.createDirectoryIfMissing(srcDir, "controller");
                VfsUtil.createDirectoryIfMissing(srcDir, "view");
                VfsUtil.createDirectoryIfMissing(srcDir, "config");
                // Crear el paquete view

                // Crear el paquete controller

            } catch (IOException e) {
                throw new RuntimeException("No se pudieron crear los paquetes MVC", e);
            }
        });
    }


    // Método principal para crear paquetes y clases
    public static void createMvcClass(Project project, String packageName, String className, boolean singleton, boolean lombok) {
        // Obtener la raíz del proyecto
        VirtualFile baseDir = project.getBaseDir();
        VirtualFile srcDir = baseDir.findChild("src");

        if (srcDir == null) {
            throw new RuntimeException("El directorio 'src' no existe.");
        }

        // Crear el paquete si no existe
        VirtualFile packageDir = srcDir.findChild(packageName);
        if (packageDir == null) {
            try {
                packageDir = VfsUtil.createDirectoryIfMissing(srcDir, packageName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Crear el archivo de la clase
        String classFileName = className + ".java";
        AtomicReference<VirtualFile> classFile = new AtomicReference<>(packageDir.findChild(classFileName));

        if (classFile.get() == null) {
            // Usamos WriteCommandAction para crear el archivo en el sistema de archivos de IntelliJ
            VirtualFile finalPackageDir = packageDir;
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    classFile.set(finalPackageDir.createChildData(null, classFileName));  // Crea el archivo sin 'this'
                } catch (IOException e) {
                    throw new RuntimeException("No se pudo crear el archivo para la clase " + className, e);
                }
            });
        }

        String classContent = "";

        if(singleton){
            if(packageName.equals("repository") || packageName.equals("service") || packageName.equals("controller")) {
                classContent = "package " + packageName + ";\npublic class " + className + " {\n" +
                        generateSingleton(className,lombok) + "   \n" +
                        "}";
            }
        }
            else
            {
                classContent = "package " + packageName + ";\npublic class " + className + " {\n" +
                        " \n" +
                        "}";
            }


        // Escribir el contenido en el archivo
        if (classFile.get() != null) {
            String finalClassContent = classContent;
            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    classFile.get().setBinaryContent(finalClassContent.getBytes()); // Escribe el contenido en el archivo
                } catch (IOException e) {
                    throw new RuntimeException("No se pudo escribir en el archivo para la clase " + className, e);
                }
            });
        }
    }

    // Método para crear varias clases en diferentes paquetes
    public static void createMvcClasses(Project project, List<String> classNames,boolean singleton, boolean lombok) {

        for (String className : classNames) {
            createMvcClass(project, "model", className + "Entity",singleton,lombok);
            createMvcClass(project, "controller", className + "Controller",singleton,lombok);
            createMvcClass(project, "service", className + "Service",singleton,lombok);
            createMvcClass(project, "repository", className + "Repository",singleton,lombok);
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

