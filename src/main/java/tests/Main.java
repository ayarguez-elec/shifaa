package tests;
import entities.PatientStatus;
import utils.DatabaseConnection;
/*import entities.medicalFile;
import services.medicalFileServ;
import entities.Prescription;
import services.Prescriptionserv;
import java.time.LocalDate;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {

        medicalFileServ mfService = new medicalFileServ();
        Prescriptionserv pService = new Prescriptionserv();





        try {
            //medicalFile

            // ADD
            medicalFile mf = new medicalFile();
            mf.setCreationdate(LocalDate.now());
            mf.setGeneralhistory("Diabète");
            mf.setAllergies("Aucune");
            mf.setAntecedents("Hypertension");
            mf.setFollowup("Suivi mensuel");
            mf.setPatientstatus(PatientStatus.stable);
            mf.setLastvisit(LocalDate.now());
            mf.setPatientId(1);

            mfService.add(mf);
            System.out.println(" MedicalFile ajouté");

            //  GET ALL
            System.out.println("\n Liste MedicalFile :");
            System.out.println(mfService.getAll());

            //  UPDATE
            mf.setIdfile(1);
            mf.setGeneralhistory("Update diabète");

            mfService.update(mf);
            System.out.println(" MedicalFile modifié");

            //  DELETE
            /*
            mfService.delete(mf);
            System.out.println(" MedicalFile supprimé");
            */


            /* prescription

            //  ADD
            Prescription p = new Prescription();
            p.setPrescriptionDate(LocalDate.now());
            p.setPrescription("Paracétamol 500mg");
            p.setTreatmentDuration("3 jours");
            p.setDoctorId(1);
            p.setPatientId(1);

            pService.add(p);
            System.out.println("Prescription ajoutée");

            // GET ALL
            System.out.println("\n Liste Prescription :");
            System.out.println(pService.getAll());

            //  UPDATE
            p.setPrescriptionId(1);
            p.setPrescription("Ibuprofène 400mg");

            pService.update(p);
            System.out.println("Prescription modifiée");

            // DELETE
            /*
            pService.delete(p);
            System.out.println("Prescription supprimée");


        } catch (SQLException e) {
            System.out.println(" SQL Error : " + e.getMessage());
        }
    }
}*/

