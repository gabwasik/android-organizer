package com.example.organizer;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Task> tasks;
    private ArrayAdapter<Task> adapter;

    // metoda tworząca widok główny
    @Override protected void onCreate(Bundle savedInstanceState) {
        Log.i("organizer", "[MainActivity] -> ➕ onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.action_bar));

        // ustawienie tytułu paska akcji
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Organizer");

        // stworzenie i wypełnienie startowej listy zadań
        tasks = new ArrayList<>();
        for (int i = 0; i < 9; i++) tasks.add(0, new Task(String.format("PRZYKŁADOWE ZADANIE %s", i+1)));
        tasks.add(0, new Task("ZADANIE Z LINKIEM DO YOUTUBE O BARDZO DŁUGIEJ NAZWIE, BO CZEMU BY NIE", "youtu.be/dQw4w9WgXcQ"));

        adapter = new ArrayAdapter<Task>(this, R.layout.task_list, tasks) {
            @NonNull @Override public View getView(int position, View taskView, @NonNull android.view.ViewGroup parent) {
                if (taskView == null) taskView = LayoutInflater.from(getContext()).inflate(R.layout.task_item, parent, false);

                // inicjalizacja elementów zadania z listy
                CheckBox checkBox = taskView.findViewById(R.id.taskCheckBox);
                TextView taskName = taskView.findViewById(R.id.taskName);
                TextView taskUrl = taskView.findViewById(R.id.taskUrl);

                // uzupełnienie elementów danymi
                final Task task = getItem(position);
                if (task != null) {
                    // widok nazwy zadania
                    if (taskName != null) taskName.setText(task.getName());

                    // widok linku do filmiku
                    if (taskUrl != null) {
                        if (task.getUrl() != null && !task.getUrl().isEmpty()) {
                            taskUrl.setText(task.getUrl());
                            taskUrl.setPaintFlags(taskUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            taskUrl.setVisibility(View.VISIBLE);
                        } else {
                            taskUrl.setVisibility(View.GONE);
                        }
                    }

                    if (checkBox != null) {
                        checkBox.setChecked(false);
                        checkBox.setOnClickListener(v -> {
                            // jeżeli zadanie zostało odznaczone, to usuwamy je z listy
                            if (((CheckBox) v).isChecked()) {
                                tasks.remove(task);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, getString(R.string.toast_task_deleted) + task.getName(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // obsługa kliknięcia na zadanie
                    taskView.setOnClickListener(v -> {
                        // jeżeli zadanie zawiera link do filmiku YouTube, to otwieramy go w aplikacji
                        if (task.getUrl() != null && !task.getUrl().isEmpty()) openYouTubeLink(task.getUrl());
                    });
                }

                return taskView;
            }
        };

        ListView listView = findViewById(R.id.task_list);
        listView.setAdapter(adapter);
    }

    // metoda inicjalizująca menu opcji
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // metoda obsługująca kliknięcia opcji menu
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_add) {
            showAddTaskDialog();
            return true;
        } else if (itemId == R.id.menu_remove) {
            Toast.makeText(MainActivity.this, R.string.menu_task_remove_help, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // metoda wyświetlająca dialog dodawania nowego zadania
    private void showAddTaskDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.task_dialog, null);
        builder.setView(dialogView);

        // inicjalizacja elementów dialogu
        final EditText editTextTaskName = dialogView.findViewById(R.id.dialogTaskName);
        final EditText editTextTaskUrl = dialogView.findViewById(R.id.dialogTaskUrl);
        TextView textViewTaskNameError = dialogView.findViewById(R.id.dialogTaskNameError);
        TextView textViewTaskUrlError = dialogView.findViewById(R.id.dialogTaskUrlError);

        builder.setTitle("Dodaj nowe zadanie")
            .setPositiveButton(R.string.button_add, null)
            .setNegativeButton(R.string.button_cancel, null);

        // stworzenie i wyświetlenie dialogu
        AlertDialog dialog = builder.create();
        dialog.show();

        // obsługa wciśnięcia przycisku "Dodaj"
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // wartości pól tekstowych dialogu
            String taskName = editTextTaskName.getText().toString().trim().toUpperCase();
            String taskUrl = editTextTaskUrl.getText().toString().trim();

            // schowanie poprzednich komunikatów błędu przy tworzeniu/odświeżaniu dialogu
            textViewTaskNameError.setVisibility(View.GONE);
            textViewTaskUrlError.setVisibility(View.GONE);

            // inicjalizacja oraz obsługa snackbara wyświetlanego przy pomyślnym dodaniu zadania
            View parentLayout = findViewById(R.id.task_list);
            Snackbar snackbar = Snackbar.make(parentLayout, R.string.toast_task_created, Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.button_undo, v2 -> {
                tasks.remove(0);
                adapter.notifyDataSetChanged();
                Snackbar.make(parentLayout, R.string.toast_task_undone, Snackbar.LENGTH_SHORT).show();
            });

            if (!taskName.isEmpty()) { // jeżeli użytkownik wprowadził nazwę zadania
                if (!taskUrl.isEmpty()) { // jeżeli użytkownik wprowadził coś w polu na link
                    // jeżeli link jest linkiem do YouTube'a
                    if (taskUrl.matches("^((?:https?:)?//)?((?:www|m)\\.)?(youtube\\.com|youtu.be)(/(?:[\\w\\-]+\\?v=|embed/|v/)?)([\\w\\-]+)(\\S+)?$")) {
                        tasks.add(0, new Task(taskName, taskUrl));
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                        snackbar.show();
                    } else {
                        textViewTaskUrlError.setText(R.string.dialog_task_url_error);
                        textViewTaskUrlError.setVisibility(View.VISIBLE);
                    }
                } else { // jeżeli użytkownik nie wpisał niczego w polu na link
                    tasks.add(0, new Task(taskName));
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                    snackbar.show();
                }
            } else { // jeżeli użytkownik nie wprowadził nazwy zadania
                textViewTaskNameError.setText(R.string.dialog_task_name_error);
                textViewTaskNameError.setVisibility(View.VISIBLE);
            }
        });
    }

    // metoda otwierająca link do filmiku w aplikacji YouTube
    private void openYouTubeLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // metody callback aktywności uzupełnione o logi wywołania
    @Override public void onStart() {
        Log.i("organizer", "[MainActivity] -> 🚀 onStart()");
        super.onStart();
    }
    @Override public void onResume() {
        Log.i("organizer", "[MainActivity] -> ▶️ onResume()");
        super.onResume();
    }
    @Override public void onPause() {
        Log.i("organizer", "[MainActivity] -> ⏸️ onPause()");
        super.onPause();
    }
    @Override public void onStop() {
        Log.i("organizer", "[MainActivity] -> 🛑 onStop()");
        super.onStop();
    }
    @Override public void onRestart() {
        Log.i("organizer", "[MainActivity] -> 🔄️ onRestart()");
        super.onRestart();
    }
    @Override public void onDestroy() {
        Log.i("organizer", "[MainActivity] -> 💣 onDestroy()");
        super.onDestroy();
    }
}