import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Devoir7Alternatives {
    // Version alternative de la question 1 avec filter au stream
    public static List<Path> dossiersEnfantAlt(Path origine, int depth) {
        if(depth < 0) return Collections.emptyList();
        if(!Files.isDirectory(origine)) throw new RuntimeException();

        List<Path> enfants = new ArrayList<>();

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(origine, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isDirectory(entry);
            }
        })) {
            for(Path p : stream) {
                enfants.add(p);
                enfants.addAll(dossiersEnfantAlt(p, depth - 1));
            }
        } catch (IOException ex) {}

        return Collections.unmodifiableList(enfants);
    }

    // Version alternative de la question 1 avec filter au stream, via lambda
    public static List<Path> dossiersEnfantAltLambda(Path origine, int depth) {
        if(depth < 0) return Collections.emptyList();
        if(!Files.isDirectory(origine)) throw new RuntimeException();

        List<Path> enfants = new ArrayList<>();

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(origine, (Path p) -> Files.isDirectory(p))) {
            for(Path p : stream) {
                enfants.add(p);
                enfants.addAll(dossiersEnfantAltLambda(p, depth - 1));
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

    // Alternative avec create directories - pas besoin de creer le repertoire de base au prealable
    public static void creeHierarchieDeProjet(Path base, String nomProjet) {
        Path repertoireProjet = base.resolve(nomProjet);
        if(Files.exists(repertoireProjet)) throw new RuntimeException();

        try {
            for (Path sub : STRUCTURE_PROJET) {
                Files.createDirectories(repertoireProjet.resolve(sub));
            }
        } catch (IOException ex) {}
    }

    //Alternative recursive
    public void supprimerProjetAltRec(Path projet) {
        if(!Devoir7.validerHierarchieProjet(projet)) throw new RuntimeException();
        try {
            supprimerRecursif(projet);
        }catch (IOException ex) {}
    }

    public void supprimerRecursif(Path supprimer) throws IOException{
        if(Files.isDirectory(supprimer)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(supprimer)) {
                for (Path file : stream) supprimerRecursif(file);
            }catch (IOException ex) {}
        }
        Files.delete(supprimer);
    }
    //Alternative avec sort - ne fonctionne pas puisque dossiersEnfant retourne une list non modifiable.
    // J'utilise un lambda ici, mais un comparator aurait pu etre utilise
    public void supprimerProjetAltSort(Path projet) {
        if(!Devoir7.validerHierarchieProjet(projet)) throw new RuntimeException();

        List<Path> subFolders = Devoir7.dossiersEnfant(projet, 10);
        subFolders.add(projet);
        Collections.sort(subFolders, (Path o1, Path o2) -> o2.getNameCount() - o1.getNameCount());

        for(Path subfolder : subFolders) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(subfolder)) {
                for (Path file : stream) Files.delete(file);
                Files.delete(subfolder);
            }catch (IOException ex) {}
        }
    }
}

