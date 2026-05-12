package services;

import java.util.List;

public interface iCrud<T> {

    void    ajouter(T t);
    void    modifier(T t);
    void    supprimer(int id);
    List<T> getAll();
    T       getById(int id);
}