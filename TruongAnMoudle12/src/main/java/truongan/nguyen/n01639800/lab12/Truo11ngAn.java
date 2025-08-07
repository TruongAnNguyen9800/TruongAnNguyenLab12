package truongan.nguyen.n01639800.lab12;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class Truo11ngAn extends Fragment {

    private TextView textViewNameId;
    private EditText editTextCourseName, editTextCourseDescription;
    private Button buttonAdd, buttonDelete;
    private RecyclerView recyclerViewCourses;
    private CourseAdapter courseAdapter;
    private List<Course> courseList = new ArrayList<>();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference coursesRef;

    private static final Pattern COURSE_NAME_PATTERN =
            Pattern.compile("^[A-Z]{4}-\\d{3,4}$");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.truo11ngan_fragment, container, false);

        textViewNameId = rootView.findViewById(R.id.textViewNameId);
        editTextCourseName = rootView.findViewById(R.id.editTextCourseName);
        editTextCourseDescription = rootView.findViewById(R.id.editTextCourseDescription);
        buttonAdd = rootView.findViewById(R.id.buttonAdd);
        buttonDelete = rootView.findViewById(R.id.buttonDelete);
        recyclerViewCourses = rootView.findViewById(R.id.recyclerViewCourses);

        textViewNameId.setText(getString(R.string.full_name_and_number));

        editTextCourseName.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;

                String upper = s.toString().toUpperCase(Locale.US);
                if (!upper.equals(s.toString())) {
                    editTextCourseName.setText(upper);
                    editTextCourseName.setSelection(upper.length());
                }

                isEditing = false;
            }
        });

        editTextCourseDescription.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        courseAdapter = new CourseAdapter(courseList);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCourses.setAdapter(courseAdapter);

        courseAdapter.setOnItemLongClickListener(position -> {
            Course courseToDelete = courseAdapter.getCourseAt(position);
            if (courseToDelete != null) {
                String key = courseToDelete.getId();
                if (key != null) {
                    coursesRef.child(key).removeValue();
                    Toast.makeText(getContext(),
                            getString(R.string.deleted_course) + courseToDelete.getName(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        coursesRef = firebaseDatabase.getReference("courses");

        buttonAdd.setOnClickListener(v -> addCourse());
        buttonDelete.setOnClickListener(v -> deleteAllCourses());

        fetchCoursesFromFirebase();

        return rootView;
    }

    private void addCourse() {
        String courseName = editTextCourseName.getText().toString().trim();
        String courseDesc = editTextCourseDescription.getText().toString().trim();

        boolean valid = true;
        if (courseName.isEmpty()) {
            editTextCourseName.setError(getString(R.string.empty));
            valid = false;
        }
        if (courseDesc.isEmpty()) {
            editTextCourseDescription.setError(getString(R.string.empty));
            valid = false;
        }
        if (!valid) return;

        if (!COURSE_NAME_PATTERN.matcher(courseName).matches()) {
            editTextCourseName.setError(getString(R.string.invalid_name));
            return;
        }

        String key = coursesRef.push().getKey();
        if (key == null) {
            Toast.makeText(getContext(), getString(R.string.error_add), Toast.LENGTH_SHORT).show();
            return;
        }

        Course newCourse = new Course(key, courseName, courseDesc);
        coursesRef.child(key).setValue(newCourse).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                editTextCourseName.setText("");
                editTextCourseDescription.setText("");
                Toast.makeText(getContext(), getString(R.string.course_added), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.add_failed), Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("FIREBASE", "Writing to: " + coursesRef.child(key).toString());
    }

    private void deleteAllCourses() {
        if (courseList.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.can_not_delete), Toast.LENGTH_SHORT).show();
            return;
        }

        coursesRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseList.clear();
                courseAdapter.setCourseList(courseList);  // assumes this method exists and calls notifyDataSetChanged()
                Toast.makeText(getContext(), getString(R.string.all_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.failed_to_deleted), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCoursesFromFirebase() {
        coursesRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Course course = snapshot.getValue(Course.class);
                if (course != null) {
                    boolean exists = false;
                    for (Course c : courseList) {
                        if (c.getId().equals(course.getId())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        courseList.add(course);
                        courseAdapter.notifyItemInserted(courseList.size() - 1);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Course updatedCourse = snapshot.getValue(Course.class);
                if (updatedCourse != null) {
                    for (int i = 0; i < courseList.size(); i++) {
                        if (courseList.get(i).getId().equals(updatedCourse.getId())) {
                            courseList.set(i, updatedCourse);
                            courseAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Course removedCourse = snapshot.getValue(Course.class);
                if (removedCourse != null) {
                    for (int i = 0; i < courseList.size(); i++) {
                        if (courseList.get(i).getId().equals(removedCourse.getId())) {
                            courseList.remove(i);
                            courseAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        getString(R.string.failed_to_load) + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
