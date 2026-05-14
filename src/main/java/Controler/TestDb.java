package Controler;

import entities.Pharmacy;
import services.PharmacyServices;

import java.util.List;

public class TestDb {
    public static void main(String[] args) {
        PharmacyServices ps = new PharmacyServices();
        List<Pharmacy> list = ps.getAll();
        System.out.println("Total pharmacies : " + list.size());
        for (Pharmacy p : list) {
            System.out.println("- " + p.getPharmacyName() + " | Address: " + p.getAddress());
        }
    }
}
