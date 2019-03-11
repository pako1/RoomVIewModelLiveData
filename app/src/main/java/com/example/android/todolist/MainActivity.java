/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.todolist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.android.todolist.database.AppDatabase;
import com.example.android.todolist.database.TaskEntry;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static com.example.android.todolist.AddTaskActivity.EXTRA_TASK_ID;


public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    private AppDatabase mDb;

    @Override
    protected void onResume() {
        super.onResume();
        //o Executor einai mia klasi poy diaxeirizetai ta threads/Runnables
        //gia na mporesoyme na paroume kai na baloyme ta dedomena apo thn db sthn o8oni
        //8a prepei na xrisimopoihsoyme ena diaforetiko thread apo to UI poy mas parexei to android.
        //gia ayto xrisimopoioyme to new Runnable kai mesa se ayto ylopoioyme to db business logic mas.
        //Distixws mesa se ayto den mporoyme na kanoyme update kai to UI dioti den mas dinei prosbasei sto
        //UI Thread gia ayto xrisimopoioyme to runOnUiThread.
        //Telos xrisimopoioume tin klasi Executers prokeimenou na mhn dimiourgh8oyn race conditions dioti
        // mporoyme na elegxsoyme thn seira twn ektelesewn twn threads.
        // des Udacity Executors android &
        // You have to use runOnUiThread() when you want to update your UI from a Non-UI Thread.
        // For eg- If you want to update your UI from a background Thread.
        // You can also use Handler for the same thing.
        //https://developer.android.com/reference/java/util/concurrent/Executor.html

        //setUpViewModel();

        // Πριν εκανε πολλες φορες το καλεσμα της βασης να δει αν ειχε αλλαξει κατι στη βαση
        // ή οταν γυρνουσαμε απο μια ακτιβιτι σε μια αλλη. Τωρα με το LiveData δεν θα καλουμε πολλες φορες
        //την βαση αλλα μονο αν εχει γινει αλλαγη σε αυτη!!

    }

    private void setUpViewModel() {
       /* AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<TaskEntry> taskEntries = mDb.taskDao().loadAllTasks();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setTasks(taskEntries);
                    }
                });

            }
        });*/
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        LiveData<List<TaskEntry>> taskEntries = mainViewModel.getTasks();
        //taskentries is of type LiveData therefore we can call it self-serve method observe.
        taskEntries.observe(MainActivity.this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(@Nullable List<TaskEntry> taskEntries) {

                //we create an anonymous class and implement the onChanged method of the interface of the Observer.
                //The observer will get notified if the observable object gets changed. (LiveData<Object...>)
                //the observer gets as a parameter the List<TaskEntry>, same thing that we are wrapping inside
                //of the LiveData object.
                //This onChanged method can access the views.

                mAdapter.setTasks(taskEntries);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDb = AppDatabase.getInstance(getApplicationContext());
        setUpViewModel();
        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int pos = viewHolder.getAdapterPosition();
                        List<TaskEntry> entryList = mAdapter.getTasks();
                        TaskEntry currentTask = entryList.get(pos);
                        AppDatabase.getInstance(getApplicationContext()).taskDao().deleteTask(currentTask);
                        // setUpViewModel(); δεν χρειαζεται να καλεσω γιατι οταν γινει
                        // η διαγραφη δεν θα αλλαξει κατι σε data για να πρεπει να ενεργοποιηθει ο observable.
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }
}
