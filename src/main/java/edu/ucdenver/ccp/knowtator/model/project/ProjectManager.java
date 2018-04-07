package edu.ucdenver.ccp.knowtator.model.project;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.io.BasicIOUtil;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLUtil;
import edu.ucdenver.ccp.knowtator.io.owl.OWLUtil;
import edu.ucdenver.ccp.knowtator.model.Savable;
import edu.ucdenver.ccp.knowtator.model.textsource.TextSource;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class ProjectManager {
    private static final Logger log = Logger.getLogger(ProjectManager.class);
    private KnowtatorManager manager;
    private File projectLocation;
    private File articlesLocation;
    private File ontologiesLocation;
    private File annotationsLocation;
    private File profilesLocation;

    public ProjectManager(KnowtatorManager manager) {
        this.manager = manager;
    }

    public File getProjectLocation() {
        return projectLocation;
    }

    public File getArticlesLocation() {
        return articlesLocation;
    }

    public void newProject(File projectDirectory) {
        makeFileStructure(projectDirectory);
    }

    public void closeProject(KnowtatorView view, File file) {
        if (view != null){
            log.warn("2.a: " + file);
            view.close(file);
        }
        else {
            log.warn("2.b: " + file);
            manager.close(file);
        }
    }

    public void loadProject(File projectFile) {
        if (projectFile != null) {
            makeFileStructure(projectFile.getParentFile());
            loadProject();
        }
    }

    public void loadProject(File profilesLocation, File ontologiesLocation, File articlesLocation, File annotationsLocation) {
        makeFileStructure(profilesLocation, ontologiesLocation, articlesLocation, annotationsLocation);
        loadProject();
    }

    private void loadProject() {
        if (ontologiesLocation != null) {
            log.warn("Loading ontologies");
            loadFromFormat(OWLUtil.class, manager.getOWLAPIDataExtractor(), ontologiesLocation);
        }

        if (profilesLocation != null) {
            log.warn("Loading profiles");
            loadFromFormat(KnowtatorXMLUtil.class, manager.getProfileManager(), profilesLocation);
        }

        if (annotationsLocation != null) {
            log.warn("Loading annotations");
            loadFromFormat(KnowtatorXMLUtil.class, manager.getTextSourceManager(), annotationsLocation);
        }

        manager.projectLoadedEvent();
    }

    /**
     Allows for manual setting of the project structure
     **/
    private void makeFileStructure(File profilesLocation, File ontologiesLocation, File articlesLocation, File annotationsLocation) {
        this.projectLocation = null;
        this.profilesLocation = profilesLocation;
        this.ontologiesLocation = ontologiesLocation;
        this.articlesLocation = articlesLocation;
        this.annotationsLocation = annotationsLocation;
    }

    private void makeFileStructure(File projectDirectory) {
        try {
            projectLocation = projectDirectory;
            articlesLocation = new File(projectDirectory, "Articles");
            ontologiesLocation = new File(projectDirectory, "Ontologies");
            annotationsLocation = new File(projectDirectory, "Annotations");
            profilesLocation = new File(projectDirectory, "Profiles");

            Files.createDirectories(projectDirectory.toPath());
            Files.createDirectories(articlesLocation.toPath());
            Files.createDirectories(ontologiesLocation.toPath());
            Files.createDirectories(annotationsLocation.toPath());
            Files.createDirectories(profilesLocation.toPath());
            if (FileUtils.listFiles(projectDirectory, new String[]{"knowtator"}, false).size() == 0)
                Files.createFile(new File(projectDirectory, projectDirectory.getName() + ".knowtator").toPath());
        } catch (IOException e) {
            System.err.println("Cannot create directories - " + e);
        }
    }

    public void saveProject() {


        if (getProjectLocation() != null) {
            //noinspection ResultOfMethodCallIgnored
            Arrays.stream(Objects.requireNonNull(profilesLocation.listFiles())).forEach(File::delete);
            //noinspection ResultOfMethodCallIgnored
            Arrays.stream(Objects.requireNonNull(annotationsLocation.listFiles())).forEach(File::delete);

            this.saveToFormat(KnowtatorXMLUtil.class, manager.getProfileManager(), profilesLocation);
            this.saveToFormat(KnowtatorXMLUtil.class, manager.getTextSourceManager(), annotationsLocation);
        }
    }

    public void addDocument(File file) {
        if (!file.getParentFile().equals(articlesLocation)) {
            try {
                FileUtils.copyFile(file, new File(articlesLocation, file.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TextSource newTextSource = manager.getTextSourceManager().addTextSource(null, file.getName());
        manager.textSourceAddedEvent(newTextSource);
    }

    public File getAnnotationsLocation() {
        return annotationsLocation;
    }

    public void saveToFormat(Class<? extends BasicIOUtil> ioClass, File file) {
        saveToFormat(ioClass, manager.getTextSourceManager(), file);
    }

    public void saveToFormat(Class<? extends BasicIOUtil> ioClass, Savable savable, File file) {
        try {
            BasicIOUtil util = ioClass.getDeclaredConstructor().newInstance();
            util.write(savable != null ? savable : manager.getTextSourceManager(), file);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromFormat(Class<? extends BasicIOUtil> ioClass, File file) {
        loadFromFormat(ioClass, manager.getTextSourceManager(), file);
    }

    public void loadFromFormat(Class<? extends BasicIOUtil> ioClass, Savable savable, File file) {
        try {
            BasicIOUtil util = ioClass.getDeclaredConstructor().newInstance();
            util.read(savable, file);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            e.printStackTrace();
        }
    }

}
