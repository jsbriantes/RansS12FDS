package com.ecca1.ranss12.ranss12fds;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "ECCA1BD";
	private static final int DATABASE_VERSION = 1;
  
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Este método se llama al crear la BD
	@Override
	public void onCreate(SQLiteDatabase database) {

        // Pilotos
        database.execSQL("CREATE TABLE PILOTOS (NPILOTO INTEGER NOT NULL PRIMARY KEY, CNOMBRE TEXT NOT NULL, CLICENCIA TEXT NOT NULL, DFECCADLIC TEXT NOT NULL," +
                         "DFECCADRECMED TEXT NOT NULL);");

        // Tipos de mantenimientos
        database.execSQL("CREATE TABLE TIPOSMANT (NTIPOMANT INTEGER NOT NULL PRIMARY KEY, CDESCRIPCION TEXT NOT NULL);");

        // Registros de mantenimiento
        database.execSQL("CREATE TABLE MANTENIMIENTOS (NMANTENIMIENTO INTEGER NOT NULL PRIMARY KEY, DFECHA TEXT NOT NULL, NHORASMOTOR INTEGER NOT NULL, " +
                         "CDESCRIPCION TEXT NOT NULL, NPILOTO INTEGER NOT NULL, NTIPOMANT INTEGER NOT NULL);");

        // Fotos asociadas a los mantenimientos, para permitir la introducción de comentarios asociados a las mismas
        database.execSQL("CREATE TABLE FOTOSMANT (NMANTENIMIENTO INTEGER NOT NULL, NORDEN INTEGER NOT NULL, CDESCRIPCION TEXT NOT NULL, PRIMARY KEY(NMANTENIMIENTO, NORDEN));");

        // Aeródromos
        database.execSQL("CREATE TABLE AERODROMOS(CICAO TEXT NOT NULL PRIMARY KEY, CNOMBRE TEXT NOT NULL);");

        // Registros de vuelos
        database.execSQL("CREATE TABLE VUELOS (NVUELO INTEGER NOT NULL PRIMARY KEY, DFECHA TEXT NOT NULL, CHORAINI TEXT NOT NULL, CHORAARR TEXT, CHORAPARO TEXT, "+
                         "CHORAFIN TEXT, FREPTNQIZQ REAL NOT NULL DEFAULT 0, FREPTNQDER REAL NOT NULL DEFAULT 0, FESTTNQIZQINI REAL NOT NULL DEFAULT 0, "+
                         "FESTTNQDERINI REAL NOT NULL DEFAULT 0, CICAOORG TEXT NOT NULL, CICAODEST TEXT, NPILOTO INTEGER NOT NULL, NCOPILOTO INTEGER, CNOMBRECOP TEXT, "+
                         "NOVERSPEED INTEGER NOT NULL DEFAULT 0, NVELOCIDADMAX INTEGER NOT NULL, NDISTANCIA INTEGER NOT NULL, NALTITUDMAX INTEGER NOT NULL DEFAULT 0, "+
                         "COBSERVACIONES TEXT, NVARIOMAXPOS INTEGER NOT NULL DEFAULT 0, NVARIOMAXNEG INTEGER NOT NULL DEFAULT 0, NVELOCIDADMED INTEGER NOT NULL DEFAULT 0, " +
                         "FRESTOTNQIZQ REAL NOT NULL DEFAULT 0, FRESTOTNQDER REAL NOT NULL DEFAULT 0);");

        // Registros detallados del vuelo
        database.execSQL("CREATE TABLE POSVUELOS(NVUELO INTEGER NOT NULL, NORDEN INTEGER NOT NULL, CLATITUD TEXT NOT NULL, CLONGITUD TEXT NOT NULL, " +
                         "NVELOCIDAD INTEGER NOT NULL DEFAULT 0, NALTITUD INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(NVUELO, NORDEN));");


        // Registros de revisiones prevuelo y postvuelo
        database.execSQL("CREATE TABLE REVVUELOS (NVUELO INTEGER NOT NULL PRIMARY KEY, COBSERVACIONES TEXT);");

        // Fotos asociadas a la revisión prevuelo o postvuelo, para permitir la introducción de comentarios
        database.execSQL("CREATE TABLE FOTOSREVVUELO (NVUELO INTEGER NOT NULL, NORDEN INTEGER NOT NULL, CDESCRIPCION TEXT NOT NULL, PRIMARY KEY(NVUELO, NORDEN));");


    }
 
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		
        // Ahora, según el número de base de datos, así vamos ejecutando
        // Paso 1 -> 2
        if (oldVersion<2) {

        }
	}	
}
