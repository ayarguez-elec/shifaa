package services;

import java.sql.SQLException;
import java.util.List;

public interface iCrud<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(int id);
    List<T> getAll();
    T getById(int id);
}

interface IServiceCrud<T> {
    void add(T var1) throws SQLException;
    void delete(T var1) throws SQLException;
    List<T> getAll() throws SQLException;
    void update(T var1) throws SQLException;
}
