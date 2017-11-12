package com.ecca1.ranss12.ranss12fds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    Button btnExit, btnPilotos, btnBackup;
    private Uri URI = null;
    DBAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        dbHelper = new DBAdapter(this);
        dbHelper.open();

        // Interface
        btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnPilotos  = (Button) findViewById(R.id.btnPilotos);
        btnPilotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Prueba").setMessage("texto de prueba");
                // Add the buttons
                builder.setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        btnBackup = (Button) findViewById(R.id.btnBackup);
        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder
                        .setTitle(getResources().getString(R.string.Backup))
                        .setMessage(getResources().getString(R.string.MakeBackupQ))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.MakeBackup), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String cfichero = "";
                                Date date = new Date();
                                SimpleDateFormat formato = new SimpleDateFormat("yyyyMMddHHmmss");
                                cfichero = "ECCA1DB_" + formato.format(date) + ".db3";
                                cfichero = cfichero.replace(" ", "0");

                                boolean lError = true;

                                try {

                                    File sd = Environment.getExternalStorageDirectory();
                                    //File data = Environment.getDataDirectory();

                                    if (sd.canWrite()) {

                                        String currentDBPath = dbHelper.getDBPath();
                                        String backupDBPath = cfichero;
                                        File currentDB = new File(currentDBPath);
                                        File backupDB = new File(sd, backupDBPath);

                                        if (currentDB.exists()) {
                                            FileChannel src = new FileInputStream(currentDB).getChannel();
                                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                                            dst.transferFrom(src, 0, src.size());
                                            src.close();
                                            dst.close();
                                            lError = false;
                                        }
                                    }

                                } catch (Exception e) {

                                }

                                if (!lError) {
                                    ArrayList<String> ficheros = new ArrayList<String>();
                                    ficheros.add(Environment.getExternalStorageDirectory() + "/" + cfichero);

                                    if (generateZipFile(ficheros, Environment.getExternalStorageDirectory().getAbsolutePath(), cfichero + ".zip")) {
                                        File filelocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+cfichero+".zip");
                                        URI = Uri.fromFile(filelocation);
                                        // Ahora preguntamos si desea enviar la copia por email
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                        alertDialogBuilder
                                                .setTitle(getString(R.string.BackupSuc))
                                                .setMessage(getString(R.string.BackupSucLg))
                                                .setCancelable(false)
                                                .setPositiveButton(getString(R.string.Send), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        try {

                                                            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                                                            emailIntent.setType("plain/text");
                                                            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "" });
                                                            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Backup ECCA1 Data Base");
                                                            if (URI != null) {
                                                                emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
                                                            }
                                                            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Backup ECCA1 Data Base");
                                                            startActivity(Intent.createChooser(emailIntent,"Sending email..."));

                                                        } catch (Throwable t) {
                                                            String cmsg = "Error. Try it again lately.";
                                                            Toast toast = Toast.makeText(MainActivity.this, cmsg, Toast.LENGTH_SHORT);
                                                            toast.setGravity(Gravity.TOP, 105, 50);
                                                            toast.show();
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        // create alert dialog
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        // show it
                                        alertDialog.show();
                                    }
                                }
                                else {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                    alertDialogBuilder
                                            .setTitle(getString(R.string.BackupError))
                                            .setMessage(getString(R.string.BackupErrorLg))
                                            .setCancelable(false)
                                            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    // create alert dialog
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    // show it
                                    alertDialog.show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });
    }

    // Generar un fichero Zip
    public static Boolean generateZipFile(ArrayList<String> sourcesFilenames, String destinationDir, String zipFilename) {
        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {
            // VER SI HAY QUE CREAR EL ROOT PATH
            boolean result = (new File(destinationDir)).mkdirs();
            String zipFullFilename = destinationDir + "/" + zipFilename;

            System.out.println(result);

            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFullFilename));
            // Compress the files
            for (String filename: sourcesFilenames) {

                FileInputStream in = new FileInputStream(filename);
                // Add ZIP entry to output stream.
                File file = new File(filename);
                out.putNextEntry(new ZipEntry(file.getName()));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // Complete the entry
                out.closeEntry();
                in.close();
            } // Complete the ZIP file
            out.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
