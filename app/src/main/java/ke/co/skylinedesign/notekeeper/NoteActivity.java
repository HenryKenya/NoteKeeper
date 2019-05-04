package ke.co.skylinedesign.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "package ke.co.skylinedesign.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    public static  final String ORIGINAL_NOTE_COURSE_ID = "package ke.co.skylinedesign.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static  final String ORIGINAL_NOTE_COURSE_TITLE = "package ke.co.skylinedesign.notekeeper.ORIGINAL_NOTE_COURSE_TITLE";
    public static  final String ORIGINAL_NOTE_COURSE_TEXT = "package ke.co.skylinedesign.notekeeper.ORIGINAL_NOTE_COURSE_TEXT";
    private NoteInfo note;
    private boolean isNewNote;
    private Spinner mSpinnerCourses;
    private EditText textNoteTitle;
    private EditText textNoteText;
    private int mNotePosition;
    private boolean isCancelling;
    private String originalNoteCourseId;
    private String originalNoteTitle;
    private String originalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // declare variable spinner
        mSpinnerCourses = findViewById(R.id.spinner_courses);

        // get list of courses
        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        // define adapter
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);

        // set dropdown resource
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // set adapter
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();

        if (savedInstanceState == null){
            saveOriginalNoteValues();
        }else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        textNoteTitle = findViewById(R.id.text_note_title);
        textNoteText = findViewById(R.id.text_note_text);
        if (isNewNote){
            createNewNote();
        }else {
            displayNote(mSpinnerCourses, textNoteTitle, textNoteText);
        }
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        originalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        originalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_TITLE);
        originalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if (isNewNote)
            return;
        originalNoteCourseId = note.getCourse().getCourseId();
        originalNoteTitle = note.getTitle();
        originalNoteText = note.getText();
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        note = dm.getNotes().get(mNotePosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCancelling){
            if (isNewNote){
                DataManager.getInstance().removeNote(mNotePosition);
            }else {
                storePreviousNoteValues();
            }
        }else {
            saveNote();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, originalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_COURSE_TITLE, originalNoteTitle);
        outState.putString(ORIGINAL_NOTE_COURSE_TEXT, originalNoteText);
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(originalNoteCourseId);
        note.setTitle(originalNoteTitle);
        note.setText(originalNoteText);
    }

    private void saveNote() {
        // set value of course in spinner
        note.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        note.setTitle(textNoteTitle.getText().toString());
        note.setText(textNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(note.getCourse());

        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(note.getTitle());
        textNoteText.setText(note.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        isNewNote = position == POSITION_NOT_SET;
        if (!isNewNote)
            note = DataManager.getInstance().getNotes().get(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }
        else if (id == R.id.action_cancel){
            isCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = textNoteTitle.getText().toString();
        String text = "Check out what I learned in the Pluralsight course \"" + course.getTitle() + "\"\n" + subject;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}
