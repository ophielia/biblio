package meg.biblio.lending.web.validator;

import meg.biblio.lending.web.model.ClassModel;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class ClassModelValidator {

    public void validateTeacherEntry(ClassModel model, BindingResult errors) {
        // check name of teacher is entered and isn't too long
        String teacherentry = model.getTeachername();
        if (teacherentry != null) {
            int length = teacherentry.trim().length();
            if (teacherentry.trim().length() == 0) {
                errors.rejectValue("teachername", "field_required");
            } else if (length > 250) {
                errors.rejectValue("teachername", "field_toolong");
            }
        } else {
            errors.rejectValue("teachername", "field_required");
        }
    }

    public void validateNewStudentEntry(ClassModel model, BindingResult errors) {
        // check name of student is entered and isn't too long
        String studententry = model.getStudentname();
        if (studententry != null) {
            int length = studententry.trim().length();
            if (studententry.trim().length() == 0) {
                errors.rejectValue("studentname", "field_required");
            } else if (length > 250) {
                errors.rejectValue("studentname", "field_toolong");
            }
        } else {
            errors.rejectValue("studentname", "field_required");
        }
    }

    public void validateEditStudentEntry(ClassModel model, BindingResult errors) {
        // check name of student is entered and isn't too long
        String studententry = model.getStudentname();
        if (studententry != null) {
            int length = studententry.trim().length();
            if (length == 0) {
                errors.rejectValue("studentname", "field_required");
            } else if (length > 250) {
                errors.rejectValue("studentname", "field_toolong");
            }
            String studentfn = model.getStudentfirstname();
            if (studentfn != null) {
                length = studentfn.trim().length();
                if (length == 0) {
                    errors.rejectValue("studentfirstname", "field_required");
                } else if (length > 250) {
                    errors.rejectValue("studentfirstname", "field_toolong");
                }
            }
        } else {
            String studentfn = model.getStudentfirstname();
            if (studentfn != null) {
                int length = studentfn.trim().length();
                if (length == 0) {
                    errors.rejectValue("studentfirstname", "field_required");
                } else if (length > 250) {
                    errors.rejectValue("studentfirstname", "field_toolong");
                }
            } else {
                errors.rejectValue("studentname", "field_required");
                errors.rejectValue("studentfirstname", "field_required");
            }


        }
    }

}
