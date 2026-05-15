package org.minimalj.frontend.form;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

@SupportedAnnotationTypes("*")
public class FormLineEnumCheckProcessor extends AbstractProcessor {

	private Trees trees;
	private Messager messager;
	private Types typeUtils;
	private TypeMirror formType;

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		try {
			trees = Trees.instance(processingEnv);
		} catch (IllegalArgumentException e) {
			return;
		}
		messager = processingEnv.getMessager();
		typeUtils = processingEnv.getTypeUtils();
		TypeElement formElement = processingEnv.getElementUtils().getTypeElement("org.minimalj.frontend.form.Form");
		if (formElement != null) {
			formType = typeUtils.erasure(formElement.asType());
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (trees == null || formType == null || roundEnv.processingOver()) {
			return false;
		}
		Set<CompilationUnitTree> visited = new HashSet<>();
		for (Element element : roundEnv.getRootElements()) {
			TreePath path = trees.getPath(element);
			if (path != null && visited.add(path.getCompilationUnit())) {
				new FormLineScanner().scan(path.getCompilationUnit(), null);
			}
		}
		return false;
	}

	private class FormLineScanner extends TreePathScanner<Void, Void> {

		@Override
		public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
			checkCall(node);
			return super.visitMethodInvocation(node, p);
		}

		private void checkCall(MethodInvocationTree node) {
			ExpressionTree select = node.getMethodSelect();
			String name;
			if (select instanceof MemberSelectTree) {
				name = ((MemberSelectTree) select).getIdentifier().toString();
			} else if (select instanceof IdentifierTree) {
				name = ((IdentifierTree) select).getName().toString();
			} else {
				return;
			}
			if (!"line".equals(name)) {
				return;
			}

			// Resolve the method element to confirm it is Form.line (or an override)
			TreePath selectPath = new TreePath(getCurrentPath(), select);
			Element methodElement = trees.getElement(selectPath);
			if (!(methodElement instanceof ExecutableElement)) {
				return;
			}
			TypeMirror declaringType = typeUtils.erasure(methodElement.getEnclosingElement().asType());
			try {
				if (!typeUtils.isSubtype(declaringType, formType)) {
					return;
				}
			} catch (IllegalArgumentException e) {
				return;
			}

			for (ExpressionTree arg : node.getArguments()) {
				TreePath argPath = new TreePath(getCurrentPath(), arg);
				TypeMirror argType = trees.getTypeMirror(argPath);
				if (argType == null || argType.getKind() == TypeKind.ERROR) {
					continue;
				}
				Element argElement = typeUtils.asElement(argType);
				if (argElement != null && argElement.getKind() == ElementKind.ENUM) {
					Element enclosing = enclosingElement();
					if (enclosing != null) {
						messager.printMessage(Diagnostic.Kind.ERROR,
								"Form.line() must not be called with an enum argument", enclosing);
					}
				}
			}
		}

		private Element enclosingElement() {
			TreePath path = getCurrentPath().getParentPath();
			while (path != null) {
				Element e = trees.getElement(path);
				if (e != null) {
					return e;
				}
				path = path.getParentPath();
			}
			return null;
		}
	}
}
