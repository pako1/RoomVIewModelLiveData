package com.example.android.todolist.database;

import java.util.Date;

public class TypeConverter {
    //epeidi den mporw na apo8ikeusw sth basi Date alla mono primitive tipes
    //prepei na to metatrepsw se Long to date otan to bazw sth basi kai otan
    //to bgazw na to kanw pali Date. H prwti me8odos einai poy pairnei apo thn basi
    //kai to kanei date kai h allh poy pairnei to antikeimeno Date kai to kanei Long

    @android.arch.persistence.room.TypeConverter
    public static Date toDate(Long timestamp) {
        Date date;
        if (timestamp == null) {
            return null;
        } else
            date = new Date(timestamp);
        return date;
    }

    @android.arch.persistence.room.TypeConverter
    public static Long toTimeStamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }


}
