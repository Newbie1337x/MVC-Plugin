package org.plugin.mvcUtil;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.util.List;


public class FileManager {

    private final Project project;
    private final boolean lombok;
    private final boolean singleton;
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
                VfsUtil.createDirectoryIfMissing(root, "service");
                VfsUtil.createDirectoryIfMissing(root, "controller");
                VfsUtil.createDirectoryIfMissing(root, "view");
                VfsUtil.createDirectoryIfMissing(root, "config");
                VfsUtil.createDirectoryIfMissing(root, "repository");
                VirtualFile repositoryDir = root.findChild("repository");
                VfsUtil.createDirectoryIfMissing(repositoryDir,"interfaces");
                VirtualFile interfaceDir = repositoryDir.findChild("interfaces");


                String className = "IActions.java";
                VirtualFile[] classFile = new VirtualFile[1];
                classFile[0] = interfaceDir.createChildData(null, className);

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    try {
                        classFile[0].setBinaryContent(generateInterface().getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException("No se pudo escribir en el archivo para la clase " + className, e);
                    }
                });


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

        if (packageName.equals("repository")) {
            // Si es repositorio, usa el generador especial que ya implementaste
            classContent = generateRepository(className);
        } else if (singleton && (packageName.equals("service") || packageName.equals("controller"))) {
            // Si es service o controller Y querés singleton
            classContent = "package " + packageName + ";\n\n" +
                    (lombok ? "import lombok.Getter;\n\n" : "") +
                    "public class " + className + " {\n" +
                    generateSingleton(className) + "\n}";
        } else {
            // Clase vacía normal
            classContent = "package " + packageName + ";\n\npublic class " + className + " {\n\n}";
        }

        String finalClassContent = classContent;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                classFile[0].setBinaryContent(finalClassContent.getBytes());
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

    public String generateSingleton(String className){

        if(lombok){
            return "\n@Getter\n" +
                    "private static final " + className + " instance = new "+ className + "();";
        }

        return "private static final " + className + " instance = new "+ className + "();";
    }


    public static String generateInterface(){
        return """
                import java.sql.SQLException;
                import java.util.List;
                import java.util.Optional;
                
                public interface IActions<T> {
                    void save(T obj) throws SQLException;
                    Optional<T> find(int id) throws SQLException;
                    List<T> findAll() throws SQLException;
                    void delete(int id) throws SQLException;
                    void update(T obj) throws SQLException;
                }
                
                """;
    }

    public String generateRepository(String className) {
        String classContent;

        // Obtener el nombre base (ej.: de PersonaRepository => Persona)
        String baseName = className.replace("Repository", "");
        String entityName = baseName + "Entity";

        // Inicia el contenido de la clase con los imports básicos
        classContent = """
                import java.sql.SQLException;
                import java.util.List;
                import java.util.Optional;
                
                """;

        // Sí se usa Lombok, añadir las importaciones correspondientes
        if (lombok) {
            classContent += "import lombok.Getter;\n\n";
        }

        // Comienza la declaración de la clase
        classContent += "public class " + className + " implements IActions<" + entityName + "> {\n\n";

        // Si la clase debe ser Singleton
        if (singleton) {
            if (lombok) {
                classContent += "    @Getter\n";
            }
            classContent += "    private static final " + className + " instance = new " + className + "();\n\n";
        }

        // Métodos de la interfaz IActions
        classContent += "    @Override\n" +
                "    public void save(" + entityName + " obj) throws SQLException {\n" +
                "        // Implement save logic\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public Optional<" + entityName + "> find(int id) throws SQLException {\n" +
                "        return Optional.empty();\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public List<" + entityName + "> findAll() throws SQLException {\n" +
                "        return List.of();\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public void delete(int id) throws SQLException {\n" +
                "        // Implement delete logic\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public void update(" + entityName + " obj) throws SQLException {\n" +
                "        // Implement update logic\n" +
                "    }\n" +
                "}";

        return classContent;
    }


}

