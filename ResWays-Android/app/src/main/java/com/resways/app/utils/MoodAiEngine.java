package com.resways.app.utils;

import com.resways.app.models.SurpriseBag;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MoodAiEngine {

    // Simulates a lightweight on-device AI by sorting based on time of day context
    public static void sortBagsByContext(List<SurpriseBag> bags) {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (currentHour >= 18) {
            Collections.sort(bags, (b1, b2) -> {
                boolean b1IsEvening = b1.getName().toLowerCase().contains("ftour") || b1.getName().toLowerCase().contains("dinner");
                boolean b2IsEvening = b2.getName().toLowerCase().contains("ftour") || b2.getName().toLowerCase().contains("dinner");
                if (b1IsEvening && !b2IsEvening) return -1;
                if (!b1IsEvening && b2IsEvening) return 1;
                return Double.compare(b1.getDistanceKm(), b2.getDistanceKm());
            });
        } 
        else if (currentHour < 12) {
            Collections.sort(bags, (b1, b2) -> {
                boolean b1IsMorning = b1.getName().toLowerCase().contains("bread") || b1.getName().toLowerCase().contains("pastry") || b1.getName().toLowerCase().contains("pastries");
                boolean b2IsMorning = b2.getName().toLowerCase().contains("bread") || b2.getName().toLowerCase().contains("pastry") || b2.getName().toLowerCase().contains("pastries");
                if (b1IsMorning && !b2IsMorning) return -1;
                if (!b1IsMorning && b2IsMorning) return 1;
                return Double.compare(b1.getDistanceKm(), b2.getDistanceKm());
            });
        } 
        else {
            Collections.sort(bags, (b1, b2) -> Double.compare(b1.getDistanceKm(), b2.getDistanceKm()));
        }
    }
}
