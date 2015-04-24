package org.minimalj.frontend.editor;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.editor.Editor.EditorListener;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.JUnitClientToolkit;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.ValidationMessage;

public class EditorTest {

	private TestEditor editor;
	private TestEditorObjectClass originalObject;
	private TestEditorObjectClass savedObject;
	private Object savedResult;
	private boolean canceled;
	
	@BeforeClass
	public static void initializeToolkit() {
		ClientToolkit.setToolkit(new JUnitClientToolkit());
	}
	
	private static JUnitClientToolkit getClientToolkit() {
		return (JUnitClientToolkit) ClientToolkit.getToolkit();
	}
	
	@Before
	public void initializeEditor() {
		editor = new TestEditor();
		editor.setEditorListener(new EditorListener() {
			@Override
			public void saved(Object savedResult) {
				EditorTest.this.savedResult = savedResult;
			}

			@Override
			public void canceled() {
				canceled = true;
			}

			@Override
			public void setValidationMessages(List<ValidationMessage> validationMessages) {
				// ignored
			}
		});
		originalObject = new TestEditorObjectClass();
	}
	
	// tests for startEditor()
	
	@Test(expected = IllegalStateException.class) public void 
	should_start_editor_twice_throws_exception() {
		editor.startEditor();
		editor.startEditor();
	}

	@Test public void 
	should_restart_editor_possible_after_save() {
		editor.startEditor();
		editor.save();
		editor.startEditor();
	}
	
	@Test public void 
	should_restart_editor_after_cancel() {
		editor.startEditor();
		editor.cancel();
		editor.startEditor();
	}

	@Test public void 
	should_without_original_object_create_new_instance() {
		originalObject = null;
		editor.startEditor();
		editor.save();
		Assert.assertTrue(savedObject instanceof TestEditorObjectClass);
	}

	@Test public void 
	should_with_original_object_make_a_clone() {
		originalObject = new TestEditorObjectClass();
		editor.startEditor();
		editor.save();
		Assert.assertFalse(savedObject == originalObject);
	}
	
	@Test public void 
	should_start_editor_creates_content() {
		editor.startEditor();
		Assert.assertNotNull(editor.getContent());
	}
	
	@Test public void 
	should_save_action_not_enabled_after_start_with_validation_errors() {
		originalObject.field = null;
		editor.startEditor();
		Assert.assertFalse(editor.saveAction.isEnabled());
	}
	
	// tests for save
	
	@Test public void 
	should_save_not_possible_with_validation_errors() {
		originalObject.field = null;
		editor.startEditor();
		// field is required so this should show an error
		editor.save();
		Assert.assertNotNull(getClientToolkit().pullError());
	}
	
	@Test public void 
	should_save_possible_with_validation_errors_in_unused_field() {
		originalObject.notShownField = null;
		editor.startEditor();
		editor.save();
		Assert.assertNull(getClientToolkit().pullError());
	}
	
	@Test public void 
	should_save_fires_saved() {
		editor.startEditor();
		editor.save();
		Assert.assertEquals(Editor.SAVE_SUCCESSFUL, savedResult);
	}
	
	@Test public void 
	should_save_finishes_editor() {
		editor.startEditor();
		editor.save();
		Assert.assertTrue(editor.isFinished());
	}
	
	// test for cancel

	@Test public void 
	should_cancel_fires_canceled() {
		editor.startEditor();
		editor.cancel();
		Assert.assertTrue(canceled);
	}
	
	@Test public void 
	should_cancel_finishes_editor() {
		editor.startEditor();
		editor.cancel();
		Assert.assertTrue(editor.isFinished());
	}

	
	private class TestEditor extends Editor<TestEditorObjectClass> {
		@Override
		protected Form<TestEditorObjectClass> createForm() {
			Form<TestEditorObjectClass> form = new Form<>();
			form.line(TestEditorObjectClass.$.field);
			return form;
		}

		@Override
		protected TestEditorObjectClass load() {
			return originalObject;
		}

		@Override
		protected Object save(TestEditorObjectClass object) throws Exception {
			EditorTest.this.savedObject = object;
			return SAVE_SUCCESSFUL;
		}
	}
	
	public static class TestEditorObjectClass {
		public static final TestEditorObjectClass $ = Keys.of(TestEditorObjectClass.class);
		
		@Size(1) @Required
		public String field = "A";
		@Size(1) @Required
		public String notShownField  = "B";
	}
}
