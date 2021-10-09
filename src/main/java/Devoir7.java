import java.awt.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Devoir7 {


    public static List<Path> dossiersEnfant(Path origine) {
        return dossiersEnfant(origine, 0);
    }

    public static List<Path> dossiersEnfant(Path origine, int depth) {
        if(depth < 0) return Collections.emptyList();
        if(!Files.isDirectory(origine)) throw new RuntimeException();

        List<Path> enfants = new ArrayList<>();

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(origine)) {
            for(Path p : stream) {
                if(Files.isDirectory(p)) {
                    enfants.add(p);
                    enfants.addAll(dossiersEnfant(p, depth - 1));
                }
            }
        } catch (IOException ex) {}

        return Collections.unmodifiableList(enfants);
    }

    public final static List<Path> STRUCTURE_PROJET = Collections.unmodifiableList(List.of(
            Paths.get("static"),
            Paths.get("static", "html"),
            Paths.get("static", "css"),
            Paths.get("static", "css", "sass"),
            Paths.get("static", "css", "build"),
            Paths.get("static", "js"),
            Paths.get("static", "img")
    ));

    public static void creeHierarchieDeProjet(Path base, String nomProjet) {
        Path repertoireProjet = base.resolve(nomProjet);

        if(Files.exists(repertoireProjet)) throw new RuntimeException();

        try {
            Files.createDirectory(repertoireProjet);
            for (Path sub : STRUCTURE_PROJET) {
                Files.createDirectory(repertoireProjet.resolve(sub));
            }
        } catch (IOException ex) {}
    }

    public static boolean validerHierarchieProjet(Path projet) {
        Set<Path> target = new HashSet<>(STRUCTURE_PROJET);
        Set<Path> current = new HashSet<>();

        for( Path p : dossiersEnfant(projet, 10)) {
            current.add(projet.relativize(p));
        }

        return target.equals(current);
    }

    public void supprimerProjet(Path projet) {
        if(!validerHierarchieProjet(projet)) throw new RuntimeException();

        // Il faut commencer par s'assurer que les dossiers sont vides avant de les supprimer.
        // On va donc commencer par le plus creux, en le vidant on va pouvoir le supprimer et ce sera un element
        // de moins a gerer pour son parent.
        // On pourrait aussi utiliser un algo recursif a la place.
        PriorityQueue<Path> subFolders = new PriorityQueue<>(new Comparator<Path>() {
            @Override
            public int compare(Path o1, Path o2) {
                // On veut le plus long en premier, donc on inverse l'ordre de comparaison.
                return  o2.getNameCount() - o1.getNameCount();
            }
        });

        // J'aurais pu sort le retour de dossierEnfant a la place de me creer un PriorityQueue, mais j'ai fait
        // en sorte que dossiersEnfant retourne un UnmodifiableList, donc je devrais la copier quand meme avant de
        // la trier.
        subFolders.addAll(dossiersEnfant(projet, 10));
        subFolders.add(projet);

        while(!subFolders.isEmpty()) {
            Path subfolder = subFolders.poll();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(subfolder)) {
                for (Path file : stream) Files.delete(file);
                Files.delete(subfolder);
            }catch (IOException ex) {}
        }
    }

    public static void copierHierarchie(Path projet, Path dst) {
        if(!validerHierarchieProjet(projet)) throw new RuntimeException();
        if(Files.exists(dst)) throw new RuntimeException();

        try {
            Files.createDirectories(dst);

            // Meme principe que pour supprimer, mais on veut le moins creux en premier.
            PriorityQueue<Path> subFolders =
                    new PriorityQueue<>((Path o1, Path o2) -> o1.getNameCount() - o2.getNameCount());
            subFolders.addAll(dossiersEnfant(projet, 10));
            subFolders.add(projet);

            while(!subFolders.isEmpty()){
                Path curFolder = subFolders.poll();
                Path targetDir = dst.resolve(projet.relativize(curFolder));

                Files.createDirectory(targetDir);

                // On cree tout les repertoires deja, donc on veut juste copier les fichiers
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(curFolder, (Path p) -> Files.isRegularFile(p) )) {
                    for(Path filePath : stream ) {
                        Path targetPath = dst.resolve(projet.relativize(filePath));
                        Files.copy(filePath, targetPath);
                    }
                }
            }
        }catch (IOException ex) {}
    }
}
