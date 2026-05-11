package services;

import entities.Prescription;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarService {

    public String generateICS(Prescription p, String patientName) {
        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Shifaa//Medical Calendar//FR\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");

        LocalDate startDate = p.getPrescriptionDate();
        int durationDays = parseDuration(p.getTreatmentDuration());
        List<String> times = parseTimes(p.getPrescription());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (String time : times) {
            ics.append("BEGIN:VEVENT\r\n");
            ics.append("SUMMARY:💊 Rappel Médicament - Shifaa\r\n");
            ics.append("DESCRIPTION:Patient: " + patientName + "\\nTraitement: " + p.getPrescription().replace("\n", " ") + "\r\n");
            
            ics.append("DTSTART:" + startDate.format(dtf) + "T" + time + "\r\n");
            ics.append("DTEND:" + startDate.format(dtf) + "T" + getEndTime(time) + "\r\n");
            ics.append("RRULE:FREQ=DAILY;COUNT=" + durationDays + "\r\n");
            ics.append("BEGIN:VALARM\r\n");
            ics.append("ACTION:DISPLAY\r\n");
            ics.append("DESCRIPTION:Rappel médicament\r\n");
            ics.append("TRIGGER:-PT15M\r\n");
            ics.append("END:VALARM\r\n");
            ics.append("END:VEVENT\r\n");
        }

        ics.append("END:VCALENDAR");
        return ics.toString();
    }

    private int parseDuration(String durationStr) {
        if (durationStr == null) return 7;
        try {
            String numeric = durationStr.replaceAll("[^0-9]", "");
            return numeric.isEmpty() ? 7 : Integer.parseInt(numeric);
        } catch (Exception e) {
            return 7;
        }
    }

    private List<String> parseTimes(String content) {
        List<String> times = new ArrayList<>();
        if (content == null) {
            times.add("080000");
            return times;
        }
        
        String lower = content.toLowerCase();
        boolean found = false;
        if (lower.contains("matin") || lower.contains("8h") || lower.contains("réveil")) {
            times.add("080000");
            found = true;
        }
        if (lower.contains("midi") || lower.contains("12h") || lower.contains("déjeuner")) {
            times.add("120000");
            found = true;
        }
        if (lower.contains("soir") || lower.contains("20h") || lower.contains("dîner")) {
            times.add("200000");
            found = true;
        }
        if (lower.contains("nuit") || lower.contains("coucher") || lower.contains("22h")) {
            times.add("220000");
            found = true;
        }
        
        if (!found) times.add("090000"); // Par défaut à 9h si rien n'est détecté
        return times;
    }

    private String getEndTime(String startTime) {
        try {
            int hour = Integer.parseInt(startTime.substring(0, 2));
            return String.format("%02d3000", hour); // 30 mins later
        } catch (Exception e) {
            return "100000";
        }
    }
}
