import entities.user;
import services.userServices;
import entities.feedback;
import services.feedbackServices;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        userServices userService = new userServices();
        feedbackServices feedbackService = new feedbackServices();

        try {
            // ADD a user
            user newUser = new user();
            /*newUser.setFirstName("Ahmed");
            newUser.setLastName("Ben Ali");
            newUser.setEmail("ahmed.benali@example.com");
            newUser.setPassword("ahmed123");
            newUser.setAddress("15 Rue de Tunis");
            newUser.setRole(user.Role.doctor);
            newUser.setAge(28);
            newUser.setGender(user.Gender.M);
            newUser.setPhone("+216 98 765 432");
            newUser.setSpecialty("Cardiology");
            newUser.setOrderNumber("LM445LKK");
            newUser.setConsultationFee(120.0f);
            newUser.setPharmacyName("");
            newUser.setLicenceNumber("");
            newUser.setUserPhoto("ahmed.jpg");
            newUser.setAccountStatus(user.AccountStatus.active);

            userService.add(newUser);
            System.out.println("User added successfully!\n");*/

            // GET ALL users
            List<user> allUsers = userService.getAll();
            /*for (user u : allUsers) {
                System.out.println("   ID: " + u.getUserId() +
                        " | Name: " + u.getFirstName() + " " + u.getLastName() +
                        " | Email: " + u.getEmail() +
                        " | Role: " + u.getRole());
            }*/


            // UPDATE a user
            // Get the last added user
            /*if (!allUsers.isEmpty()) {
                user userToUpdate = allUsers.get(allUsers.size() - 1);
                userToUpdate.setFirstName("Mourad");
                userToUpdate.setLastName("Ben Salah");
                userToUpdate.setAge(29);
                userToUpdate.setPhone("+216 55 555 555");

                userService.update(userToUpdate);
                System.out.println("User updated successfully!");
            }*/


            // ADD a feedback
            feedback newFeedback = new feedback();
            /*newFeedback.setUserId(1);  // Assuming patient ID 1 exists
            newFeedback.setTargetType(feedback.TargetType.doctor);
            newFeedback.setTargetId(13);  // Assuming doctor ID 2 exists
            newFeedback.setRating(5);
            newFeedback.setComment("Excellent doctor! Very professional and caring.");
            newFeedback.setCreationDate(new Timestamp(System.currentTimeMillis()));
            newFeedback.setAnonyme(false);

            feedbackService.add(newFeedback);
            System.out.println("Feedback added successfully!\n");*/

            // GET ALL feedbacks
            List<feedback> allFeedbacks = feedbackService.getAll();
            /*for (feedback f : allFeedbacks)
            {
                System.out.println("   ID: " + f.getFeedbackId() +
                        " | From User ID: " + f.getUserId() +
                        " | Target: " + f.getTargetType() + " ID: " + f.getTargetId() +
                        " | Rating: " + f.getRating() + "/5" +
                        " | Comment: " + (f.getComment()));
            }*/


            // UPDATE a feedback
            /*if (!allFeedbacks.isEmpty()) {
                feedback feedbackToUpdate = allFeedbacks.get(allFeedbacks.size() - 1);
                feedbackToUpdate.setRating(1);
                feedbackToUpdate.setComment("not recommended!");
                feedbackToUpdate.setAnonyme(true);

                feedbackService.update(feedbackToUpdate);
                System.out.println("Feedback updated successfully!");
            }*/

            // DELETE a feedback

            /*if (!allFeedbacks.isEmpty()) {
                feedback feedbackToDelete = allFeedbacks.get(allFeedbacks.size() - 1);
                feedbackService.delete(feedbackToDelete);
                System.out.println("Feedback deleted successfully!\n");
            }
            */

            // DELETE a user (commented by default)
            /*if (!allUsers.isEmpty()) {
                user userToDelete = allUsers.get(allUsers.size() - 1);
                userService.delete(userToDelete);
                System.out.println("User deleted successfully!\n");
            }*/


        } catch (SQLException e) {
            System.out.println("SQL Error : " + e.getMessage());
        }
    }
}