package com.hbvhuwe.goals;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hbvhuwe.goals.adapters.BaseAdapter;
import com.hbvhuwe.goals.adapters.StagesAdapter;
import com.hbvhuwe.goals.model.Goal;
import com.hbvhuwe.goals.model.Stage;
import com.hbvhuwe.goals.providers.DataProvider;
import com.hbvhuwe.goals.providers.SQLiteProvider;
import com.hbvhuwe.goals.providers.db.DbHelper;
import com.hbvhuwe.goals.swipe.StageSwipeListener;
import com.hbvhuwe.goals.swipe.SwipeHelper;

import java.util.Objects;

public class StagesActivity extends AppCompatActivity implements StageSwipeListener,
        StagesAdapter.StageCheckedListener {
    private int goalId;
    private DataProvider provider;

    private EditText goalTitle, goalDesc;
    private TextView goalCreated;
    private ProgressBar goalProgress;
    private RecyclerView stagesList;
    private ImageButton addStage;

    private CoordinatorLayout stagesLayout;

    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stages);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        provider = new SQLiteProvider(new DbHelper(getApplicationContext()));

        goalTitle = findViewById(R.id.goal_title);
        goalDesc = findViewById(R.id.goal_desc);
        goalCreated = findViewById(R.id.goal_created);
        goalProgress = findViewById(R.id.goal_progress);
        stagesList = findViewById(R.id.stages_list);
        addStage = findViewById(R.id.add_stage);
        stagesLayout = findViewById(R.id.stages_layout);

        goalTitle.setSelected(false);

        if (savedInstanceState != null) {
            goalId = savedInstanceState.getInt("goalId", 0);
        } else {
            goalId = getIntent().getIntExtra("goalId", 0);
        }

        initGoal();

        initStages();

        addStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAdd();
            }
        });
    }

    private void initGoal() {
        Goal goal = provider.getGoalById(goalId);
        goalTitle.setText(goal.getTitle());
        goalDesc.setText(goal.getDesc());
        goalProgress.setProgress((int) goal.getPercent());
        goalCreated.setText(goal.getCreated());
    }

    private void initStages() {
        adapter = new StagesAdapter(provider.getStages(goalId), this);

        stagesList.setLayoutManager(new LinearLayoutManager(this));
        stagesList.setItemAnimator(new DefaultItemAnimator());
        stagesList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        stagesList.setNestedScrollingEnabled(false);
        stagesList.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback helper = new SwipeHelper(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(helper).attachToRecyclerView(stagesList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("goalId", goalId);
    }

    @Override
    public void onChecked(int stageId, boolean isChecked) {
        provider.checkStage(goalId, stageId, isChecked);
    }

    @Override
    public void onSwipe(final Stage stage, int direction, final int position) {
        provider.deleteStageById(goalId, stage.getStageId());
        adapter.deleteItem(position);

        Snackbar undo = Snackbar.make(stagesLayout, stage.getTitle() + " removed!", Snackbar.LENGTH_LONG);
        undo.setAction(R.string.undo_action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provider.addStage(goalId, stage);
                adapter.addItem(stage, position);
            }
        });
        undo.setActionTextColor(Color.YELLOW);
        undo.show();
    }

    private void onAdd() {
        final View dialogView = getLayoutInflater().inflate(R.layout.add_stage_dialog, null);
        final EditText stageTitle = dialogView.findViewById(R.id.stage_dialog_title);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.stage_dialog_title)
                .setMessage(R.string.stage_dialog_message)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Stage stage = new Stage();
                        stage.setTitle(stageTitle.getText().toString().trim());
                        stage.setGoalId(goalId);
                        stage.setCompleted(false);
                        provider.addStage(goalId, stage);
                        adapter.addItem(stage, 0);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
        dialog.show();
    }

    private void onTitleUpdated() {
        // TODO: 18/06/18 implement goal title update
    }

    private void onDescUpdated() {
        // TODO: 18/06/18 implement goal description update
    }
}
