import entities.Reclamation;
import entities.Reponse;
import services.ReclamationServices;
import services.ReponseServices;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        ReclamationServices reclamationServices = new ReclamationServices();
        ReponseServices reponseServices = new ReponseServices();

        System.out.println("===== TEST RECLAMATION =====");

        Reclamation reclamation = new Reclamation(
                "Problème test",
                "Ceci est une réclamation de test",
                new Date(System.currentTimeMillis()),
                "En attente",
                "Urgente",
                1
        );

        reclamationServices.add(reclamation);

        List<Reclamation> reclamations = reclamationServices.getAll();

        for (Reclamation r : reclamations) {
            System.out.println(
                    r.getReclamationId() + " | " +
                            r.getSubject() + " | " +
                            r.getDescription() + " | " +
                            r.getStatusreclam() + " | " +
                            r.getPriority()
            );
        }

        System.out.println("===== TEST REPONSE =====");

        if (!reclamations.isEmpty()) {
            int reclamationId = reclamations.get(0).getReclamationId();

            Reponse reponse = new Reponse(
                    "Réponse admin de test",
                    new Timestamp(System.currentTimeMillis()),                 1,
                    reclamationId
            );

            reponseServices.add(reponse);

            List<Reponse> reponses = reponseServices.getAll();

            for (Reponse rep : reponses) {
                System.out.println(
                        rep.getResponseId() + " | " +
                                rep.getContent() + " | " +
                                rep.getResponseDate() + " | Reclamation ID: " +
                                rep.getReclamationId()
                );
            }
        }
    }
}