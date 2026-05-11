package services;
import java.sql.SQLException;
import java.util.List;
public interface iCrud<T> {
    void add(T var1) throws SQLException;

    void deleteById(int id) throws SQLException;

    List<T> getAll() throws SQLException;

    void update(T var1) throws SQLException;
}
