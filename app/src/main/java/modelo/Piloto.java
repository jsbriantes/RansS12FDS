package modelo;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ecca1.ranss12.ranss12fds.DBAdapter;

public class Piloto {

    int nPiloto;
    String cNombre;
    String cLicencia;
    Date dFecCadLic;
    Date dFecCadRecMed;

    static DBAdapter dbHelper;

    public Piloto() {
        super();
        nPiloto = -1;
    }

    public Piloto(String cNombre, String cLicencia, Date dFecCadLic, Date dFecCadRecMed) {
        super();
        this.cNombre = cNombre;
        this.cLicencia = cLicencia;
        this.dFecCadLic = dFecCadLic;
        this.dFecCadRecMed = dFecCadRecMed;
    }

    //Método estático que asigna la conexión
    public static void setDBAdapter(DBAdapter dbHelperP){
        dbHelper = dbHelperP;
    }

    static public Piloto GetPilotoById(int nPiloto) {

        Piloto piloto = new Piloto();

        String cSql = "select * from pilotos where npiloto = " + String.valueOf(nPiloto);
        Cursor c = dbHelper.ConsultaSQL(cSql, null);

        if (c.moveToFirst()) {
            if (!c.isNull(c.getColumnIndexOrThrow("NPILOTO"))) {
                piloto.setnPiloto(nPiloto);
                piloto.setcNombre(c.getString(c.getColumnIndexOrThrow("CNOMBRE")));
                piloto.setcLicencia(c.getString(c.getColumnIndexOrThrow("CLICENCIA")));
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    piloto.setdFecCadLic(formato.parse(c.getString(c.getColumnIndexOrThrow("DFECCADLIC"))));
                    piloto.setdFecCadRecMed(formato.parse(c.getString(c.getColumnIndexOrThrow("DFECCADRECMED"))));
                } catch (Exception e) {
                }
            }
        }
        c.close();
        return piloto;
    }

    public Piloto GetPilotoByNombre(String cNombre) {

        Piloto piloto = new Piloto();

        if (cNombre.length()>0) {

            String cSql = "select * from pilotos where cnombre = '" + cNombre + "'";
            Cursor c = dbHelper.ConsultaSQL(cSql, null);

            if (c.moveToFirst()) {
                if (!c.isNull(c.getColumnIndexOrThrow("NPILOTO"))) {
                    piloto.setnPiloto(nPiloto);
                    piloto.setcNombre(c.getString(c.getColumnIndexOrThrow("CNOMBRE")));
                    piloto.setcLicencia(c.getString(c.getColumnIndexOrThrow("CLICENCIA")));
                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        piloto.setdFecCadLic(formato.parse(c.getString(c.getColumnIndexOrThrow("DFECCADLIC"))));
                        piloto.setdFecCadRecMed(formato.parse(c.getString(c.getColumnIndexOrThrow("DFECCADRECMED"))));
                    } catch (Exception e) {
                    }
                }
            }
            c.close();
        }

        return piloto;
    }

    //Método que guarda creando el objeto en la base de datos
    public void save()
    {

        if (nPiloto<=0) {
            // Localizar un número de piloto al máximo más uno
            nPiloto = 1;
            Cursor c = dbHelper.ConsultaSQL("select max(npiloto) from pilotos",null);
            if (c.moveToFirst())
                if (!c.isNull(0))
                    nPiloto = c.getInt(0)+1;
            c.close();
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        String Cquery = "INSERT INTO PILOTOS(NPILOTO, CNOMBRE, CLICENCIA, DFECCADLIC, DFECCADRECMED) "+
                " VALUES("+String.valueOf(nPiloto)+",'"+cNombre+"','"+cLicencia+"','"+
                    formato.format(dFecCadLic)+"','"+formato.format(dFecCadRecMed)+"')";

        //Insertamos el nuevo articulo
        dbHelper.ExecQuerySinResult(Cquery);
    }

    //Método que actualiza la base de datos con los datos del piloto
    public void update()
    {

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        //Creamos la consulta
        String cSql = "UPDATE PILOTOS SET CNOMBRE='"+this.getcNombre()+"',"
                + "	CLICENCIA='"+this.getcLicencia()+"',"
                + "	DFECCADLIC='"+ formato.format(this.getdFecCadLic())+"',"
                + "	DFECCADRECMED='"+formato.format(this.getdFecCadRecMed())+"'"
                + " WHERE NPILOTO="+String.valueOf(this.getnPiloto());

        dbHelper.ExecQuerySinResult(cSql);
    }


    public int getnPiloto() {
        return nPiloto;
    }

    public void setnPiloto(int nPiloto) {
        this.nPiloto = nPiloto;
    }

    public String getcNombre() {
        return cNombre;
    }

    public void setcNombre(String cNombre) {
        this.cNombre = cNombre;
    }

    public String getcLicencia() {
        return cLicencia;
    }

    public void setcLicencia(String cLicencia) {
        this.cLicencia = cLicencia;
    }

    public Date getdFecCadLic() {
        return dFecCadLic;
    }

    public void setdFecCadLic(Date dFecCadLic) {
        this.dFecCadLic = dFecCadLic;
    }

    public Date getdFecCadRecMed() {
        return dFecCadRecMed;
    }

    public void setdFecCadRecMed(Date dFecCadRecMed) {
        this.dFecCadRecMed = dFecCadRecMed;
    }
}
