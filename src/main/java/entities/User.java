package entities;

public class User {
    private int id;
    private String email;
    private String passwordHash;
    private String role;
    private String profileImagePath; // relative path to profile image

    public User(int id, String email, String passwordHash, String role, String profileImagePath) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profileImagePath = profileImagePath;
    }

    public User(String email, String passwordHash, String role, String profileImagePath) {
        this(0, email, passwordHash, role, profileImagePath);
    }

    // Getters
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getProfileImagePath() { return profileImagePath; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role='" + role + "', profileImagePath='" + profileImagePath + "'}";
    }
}
