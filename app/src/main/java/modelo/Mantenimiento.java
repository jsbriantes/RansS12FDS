package modelo;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ecca1.ranss12.ranss12fds.DBAdapter;


public class Mantenimiento {

    int nMantenimiento;
    Date dFecha;
    int nHorasMotor;
    String cDescripcion;
    Piloto piloto;
    int nTipoMant;

    static DBAdapter dbHelper;

    public Mantenimiento() {
        super();
        nMantenimiento = -1;
    }

    public Mantenimiento(int nMantenimiento, Date dFecha, int nHorasMotor, String cDescripcion, Piloto piloto, int nTipoMant) {
        super();
        this.nMantenimiento = nMantenimiento;
        this.dFecha = dFecha;
        this.nHorasMotor = nHorasMotor;
        this.cDescripcion = cDescripcion;
        this.piloto = piloto;
        this.nTipoMant = nTipoMant;
    }

    //Método estático que asigna la conexión
    public static void setDBAdapter(DBAdapter dbHelperP){
        dbHelper = dbHelperP;
    }

    public Mantenimiento GetMantenimientoById(int nMantenimiento) {
        Mantenimiento mantenimiento = new Mantenimiento();

        String cSql = "select * from mantenimientos where NMANTENIMIENTO = " + String.valueOf(nMantenimiento);
        Cursor c = dbHelper.ConsultaSQL(cSql, null);

        if (c.moveToFirst()) {
            if (!c.isNull(c.getColumnIndexOrThrow("NMANTENIMIENTO"))) {
                mantenimiento.setnMantenimiento(nMantenimiento);
                mantenimiento.setcDescripcion(c.getString(c.getColumnIndexOrThrow("CDESCRIPCION")));
                mantenimiento.setnHorasMotor(c.getInt(c.getColumnIndexOrThrow("NHORASMOTOR")));
                mantenimiento.setnTipoMant(c.getInt(c.getColumnIndexOrThrow("NTIPOMANT")));
                mantenimiento.setPiloto(Piloto.GetPilotoById(c.getInt(c.getColumnIndexOrThrow("NPILOTO"))));
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    mantenimiento.setdFecha(formato.parse(c.getString(c.getColumnIndexOrThrow("DFECHA"))));
                } catch (Exception e) {
                }
            }
        }
        c.close();
        return mantenimiento;
    }

    //Método que guarda creando el objeto en la base de datos
    public void save()
    {
        if (nMantenimiento<=0) {
            // Localizar un número de mantenimiento al máximo más uno
            nMantenimiento = 1;
            Cursor c = dbHelper.ConsultaSQL("select max(NMANTENIMIENTO) from MANTENIMIENTOS",null);
            if (c.moveToFirst())
                if (!c.isNull(0))
                    nMantenimiento = c.getInt(0)+1;
            c.close();
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        String Cquery = "INSERT INTO MANTENIMIENTOS(NMANTENIMIENTO, DFECHA, NHORASMOTOR, CDESCRIPCION, NPILOTO, NTIPOMANT) "+
                " VALUES("+String.valueOf(nMantenimiento)+",'"+formato.format(this.getdFecha())+"',"+String.valueOf(this.nHorasMotor)+",'"+
                    this.cDescripcion+"',"+String.valueOf(this.piloto.nPiloto)+","+String.valueOf(this.nTipoMant)+")";

        //Insertamos el nuevo articulo
        dbHelper.ExecQuerySinResult(Cquery);
    }

    //Método que actualiza la base de datos con los datos del piloto
    public void update()
    {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        //Creamos la consulta
        String cSql = "UPDATE MANTENIMIENTOS SET CDESCRIPCION='"+this.getcDescripcion()+"',"
                + "	DFECHA='"+formato.format(this.getdFecha())+"',"
                + "	NHORASMOTOR="+String.valueOf(this.getnHorasMotor())+","
                + "	NPILOTO="+String.valueOf(this.getPiloto().nPiloto)+","
                + " NTIPOMANT="+String.valueOf(this.getnTipoMant())
                + " WHERE NMANTENIMIENTO="+String.valueOf(this.getnMantenimiento());

        dbHelper.ExecQuerySinResult(cSql);
    }

    public int getnMantenimiento() {
        return nMantenimiento;
    }

    public void setnMantenimiento(int nMantenimiento) {
        this.nMantenimiento = nMantenimiento;
    }

    public Date getdFecha() {
        return dFecha;
    }

    public void setdFecha(Date dFecha) {
        this.dFecha = dFecha;
    }

    public int getnHorasMotor() {
        return nHorasMotor;
    }

    public void setnHorasMotor(int nHorasMotor) {
        this.nHorasMotor = nHorasMotor;
    }

    public String getcDescripcion() {
        return cDescripcion;
    }

    public void setcDescripcion(String cDescripcion) {
        this.cDescripcion = cDescripcion;
    }

    public Piloto getPiloto() {
        return piloto;
    }

    public void setPiloto(Piloto piloto) {
        this.piloto = piloto;
    }

    public int getnTipoMant() {
        return nTipoMant;
    }

    public void setnTipoMant(int nTipoMant) {
        this.nTipoMant = nTipoMant;
    }
}
