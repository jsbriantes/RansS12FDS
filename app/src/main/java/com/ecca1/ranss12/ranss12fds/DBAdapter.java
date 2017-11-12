package com.ecca1.ranss12.ranss12fds;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DBAdapter {
	private Context context;
	private static SQLiteDatabase database;
	private DataBaseHelper dbHelper;
 
	public DBAdapter(Context context) {
		this.context = context;
	}
	
	public DBAdapter open() throws SQLException {
		dbHelper = new DataBaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}
 
	public void close() {
		dbHelper.close();
	}	
	
	public int getDBVersion() {
		return database.getVersion();
	}

	public String getDBPath() {
		return context.getDatabasePath(dbHelper.getDatabaseName().toString()).toString();
	}
	
	public Cursor MostrarTodos(String cTabla, String[] cCampos, String cWhere) throws SQLException {
		
		Cursor mCursor = database.query(cTabla, cCampos,
                                        cWhere, null, null, null, null); 		
		if (mCursor != null) {
		   mCursor.moveToFirst();
		}
			return mCursor;
		}
	
	public int ActualizarRegistro(String cTabla, ContentValues args, String cCondicion) {
		
		return database.update(cTabla, args, cCondicion, null);						
	}

    public long InsertarActualizarRegistro(String cTabla, ContentValues args, String cPrimaryKey) {
        return database.insertWithOnConflict(cTabla, cPrimaryKey, args, SQLiteDatabase.CONFLICT_REPLACE);
    }
	
	
	public long InsertarRegistro(String cTabla, ContentValues args, String cPrimaryKey) {
		
		return  database.insert(cTabla, cPrimaryKey, args);
				
	}
	
	public int BorrarTabla(String cTabla, String cWhere, String[] cWhereArgs ){
		
		return database.delete(cTabla, cWhere , cWhereArgs);		
	}
	
	// Para ejecutar consultas SQL que no sean SELECT
	public void ExecQuerySinResult(String cSql) {		
		database.execSQL(cSql);		
	}
	
	// Consulta SQL gen�rica que devuelve un cursor
	public Cursor ConsultaSQL(String cSql, String[] selArgs) {
		
		Cursor mCursor = database.rawQuery(cSql, selArgs);
		
		if (mCursor != null) mCursor.moveToFirst();		
		
		return mCursor;		
	}
	
	// Iniciar transacciones
	public void StartTransaction() {
		if (database.inTransaction()) database.endTransaction();
		database.beginTransaction();
	}	
	
	// Commit transacci�n
	public void Commit() {
		if (database.inTransaction()) 
		{
			database.setTransactionSuccessful();
			database.endTransaction();
		}
	}
	
	// Rollback work
	public void RollBack() {
		if (database.inTransaction()) 
		{
			database.endTransaction();
		}		
	}
}

