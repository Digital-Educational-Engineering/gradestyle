package gradestyle.validator;

public enum Type {
  Formatting_LineLength,
  Formatting_FileName,
  Formatting_TopLevelClass,
  Formatting_ImportStar,
  Formatting_MissingImportSeparator,
  Formatting_ExcessImportSeparator,
  Formatting_SingleLine,
  Formatting_LineWrapping,
  Formatting_MultiLine,
  Formatting_AnnotationLocation,
  Formatting_EmptyLine,
  Formatting_IndentationSize,
  Formatting_IndentationTabs,
  Formatting_Padding,
  Formatting_BracePlacement,
  Formatting_MissingBraces,
  Formatting_MultipleDeclarations,
  Formatting_ArrayStyle,
  Formatting_FallThrough,
  Formatting_UpperEll,
  Formatting_ModifierOrder,
  Formatting_EscapedCharacter,
  Formatting_UnicodeCharacter,
  Formatting_OneTopLevel,

  ClassNames_Regex,
  ClassNames_Noun,
  ClassNames_File,
  ClassNames_Abbreviation,

  MethodNames_Regex,
  MethodNames_Verb,
  MethodNames_Finalize,
  MethodNames_Abbreviation,

  VariableNames_Regex,
  VariableNames_FinalStaticUppercase,
  VariableNames_Boolean,
  VariableNames_Abbreviation,
  VariableNames_TypeRegex,

  PackageNames_Regex,
  PackageNames_File,

  Commenting_Meaningful,
  Commenting_FrequencyLow,
  Commenting_FrequencyHigh,

  JavadocClass_Missing,
  JavadocMethod_Missing,
  JavadocConstructor_Missing,
  JavadocField_Missing,

  Javadoc_Invalid,
  Javadoc_MissingTag,
  Javadoc_ExtraTag,
  Javadoc_MissingTagDescription,
  Javadoc_TagDescriptionIndentation,
  Javadoc_MissingSummary,
  Javadoc_SummaryLength,
  Javadoc_Paragraph,
  Javadoc_EmptyLine,
  Javadoc_TagOrder,
  Javadoc_SingleLine,
  Javadoc_Asterisk,
  Javadoc_Padding,
  Javadoc_Position,
  Javadoc_MissingPeriod,

  PrivateInstances,

  Ordering_StaticFields,
  Ordering_StaticMethods,
  Ordering_InstanceFields,
  Ordering_Constructors,
  Ordering_InstanceMethods,
  Ordering_Overloaded,
  Ordering_Import,
  Ordering_DeclarationUsage,
  Ordering_InnerClasses,

  Useless_EmptyBlock,
  Useless_Assignment,
  Useless_LocalVariable,
  Useless_Field,
  Useless_Method,
  Useless_Cast,
  Useless_Constructor,
  Useless_FullyQualifiedName,
  Useless_Import,
  Useless_Return,
  Useless_CommentedCode,

  StringConcatenation,

  Clones,

  JavaFX_Controller,
  JavaFX_FieldAnnotation,
  JavaFX_Initializer,
  JavaFX_EventHandlerName,
  JavaFX_EventHandlerAnnotation,
  JavaFX_EventHandlerPrivate,

  MissingOverride,
  EmptyCatchBlock,
  UnqualifiedStaticAccess_Method,
  UnqualifiedStaticAccess_Field,
  FinalizeOverride;

  public Category getCategory() {
    return Category.valueOf(name().split("_")[0]);
  }

  public String getMessage() {
    switch (this) {
      case Formatting_LineLength:
        return "This line has too many characters.";
      case Formatting_FileName:
        return "The file name does not match the class name.";
      case Formatting_TopLevelClass:
        return "Each file must contain only one top level class.";
      case Formatting_ImportStar:
        return "Imports should not use a wild card.";
      case Formatting_MissingImportSeparator:
        return "This import should be separated.";
      case Formatting_ExcessImportSeparator:
        return "This import is not correctly separated.";
      case Formatting_SingleLine:
        return "This statement should be on a single line.";
      case Formatting_LineWrapping:
        return "This statement is not correctly line wrapped.";
      case Formatting_MultiLine:
        return "This statement should be split over multiple lines.";
      case Formatting_AnnotationLocation:
        return "This annotation is not correctly positioned.";
      case Formatting_EmptyLine:
        return "There should be exactly one empty line between items.";
      case Formatting_IndentationSize:
      case Formatting_IndentationTabs:
        return "This line is not correctly indented.";
      case Formatting_Padding:
        return "This statement is not correctly padded with whitespace.";
      case Formatting_BracePlacement:
        return "This brace is not correctly placed.";
      case Formatting_MissingBraces:
        return "This statement is missing braces.";
      case Formatting_MultipleDeclarations:
        return "These declarations should be seperate.";
      case Formatting_ArrayStyle:
        return "This array declaration uses C style syntax, not Java style syntax.";
      case Formatting_FallThrough:
        return "This case statement should not fall through to the next case without"
            + " documentation.";
      case Formatting_UpperEll:
        return "This long constant should not use the lowercase ell suffix.";
      case Formatting_ModifierOrder:
        return "These modifiers are in the incorrect order.";
      case Formatting_EscapedCharacter:
      case Formatting_UnicodeCharacter:
        return "This character is not escaped correctly.";
      case Formatting_OneTopLevel:
        return "There should be only one top level class per file.";

      case ClassNames_Regex:
        return "This class name does not match the naming convention.";
      case ClassNames_Noun:
        return "This class name does not contain a noun.";
      case ClassNames_File:
        return "This class name does not match the file name.";
      case ClassNames_Abbreviation:
        return "This class name contains an abbreviation.";

      case MethodNames_Regex:
        return "This method name does not match the naming convention.";
      case MethodNames_Verb:
        return "This method name does not contain a verb.";
      case MethodNames_Finalize:
        return "Method names must not be named finalize.";
      case MethodNames_Abbreviation:
        return "This method name contains an abbreviation.";

      case VariableNames_Regex:
      case VariableNames_FinalStaticUppercase:
      case VariableNames_Boolean:
        return "This variable name does not match the naming convention.";
      case VariableNames_Abbreviation:
        return "This variable name contains an abbreviation.";
      case VariableNames_TypeRegex:
        return "This type variable name does not match the naming convention.";

      case PackageNames_Regex:
        return "This package name does not match the naming convention.";
      case PackageNames_File:
        return "This package name does not match the directory structure.";

      case Commenting_Meaningful:
        return "This comment is very similar to the code it is about.";
      case Commenting_FrequencyLow:
        return "This method is not commented enough.";
      case Commenting_FrequencyHigh:
        return "This method is over-commented.";

      case JavadocClass_Missing:
        return "The Javadoc comment for the class is missing.";
      case JavadocMethod_Missing:
        return "The Javadoc comment for the method is missing.";
      case JavadocConstructor_Missing:
        return "The Javadoc comment for the constructor is missing.";
      case JavadocField_Missing:
        return "The Javadoc comment for the field is missing.";

      case Javadoc_Invalid:
        return "This Javadoc comment format is incorrect.";
      case Javadoc_MissingTag:
        return "This Javadoc is missing a tag.";
      case Javadoc_ExtraTag:
        return "This Javadoc has an extra tag.";
      case Javadoc_MissingTagDescription:
        return "This Javadoc tag description is missing.";
      case Javadoc_TagDescriptionIndentation:
        return "This Javadoc tag description is not indented correctly.";
      case Javadoc_MissingSummary:
        return "This Javadoc is missing a summary.";
      case Javadoc_SummaryLength:
        return "This Javadoc summary is too short.";
      case Javadoc_Paragraph:
        return "This Javadoc paragraph is not preceded by a blank line.";
      case Javadoc_EmptyLine:
        return "The Javadoc tags are not preceded by a blank line.";
      case Javadoc_TagOrder:
        return "The Javadoc tags are not in the correct order.";
      case Javadoc_SingleLine:
        return "This Javadoc should be a single line.";
      case Javadoc_Asterisk:
        return "This Javadoc is missing a leading asterisk.";
      case Javadoc_Padding:
        return "This Javadoc does not have whitespace after the asterisks.";
      case Javadoc_Position:
        return "This Javadoc is not in the correct position.";
      case Javadoc_MissingPeriod:
        return "This Javadoc is missing a period at the end.";

      case PrivateInstances:
        return "This instance should have a different access modifier.";

      case Ordering_StaticFields:
        return "Static field '%s' is out of order. It should appear after '%s'.";
      case Ordering_StaticMethods:
        return "Static method '%s' is out of order. It should appear after '%s'.";
      case Ordering_InstanceFields:
        return "Instance field '%s' is out of order. It should appear after '%s'.";
      case Ordering_Constructors:
        return "Constructor '%s' is out of order. It should appear after '%s'.";
      case Ordering_InstanceMethods:
        return "Instance method '%s' is out of order. It should appear after '%s'.";
      case Ordering_Overloaded:
        return "Overloaded method '%s' is out of order. It should appear after '%s'.";
      case Ordering_Import:
        return "Import is out of order. Ensure imports are alphabetically ordered.";
      case Ordering_DeclarationUsage:
        return "Declaration '%s' is out of order. It should appear after '%s'.";
      case Ordering_InnerClasses:
        return "Inner class '%s' is out of order. It should appear after '%s'.";

      case Useless_EmptyBlock:
        return "This block of code is empty.";
      case Useless_Assignment:
        return "This assignment is not used.";
      case Useless_LocalVariable:
        return "This variable is not used.";
      case Useless_Field:
        return "This field is not used.";
      case Useless_Method:
        return "This method is not used.";
      case Useless_Cast:
        return "This cast is redundant.";
      case Useless_Constructor:
        return "This constructor is equivalent to the default constructor.";
      case Useless_FullyQualifiedName:
        return "This fully qualified type is already imported.";
      case Useless_Import:
        return "This import is not used.";
      case Useless_Return:
        return "This return is redundant.";
      case Useless_CommentedCode:
        return "This comment is code.";

      case EmptyCatchBlock:
        return "This catch block should not be empty";

      case StringConcatenation:
        return "This assignment concatenates a string inside a loop. Use StringBuilder instead";

      case Clones:
        return "This code is cloned.";

      case JavaFX_Controller:
        return "This controller class does not match the naming convention.";
      case JavaFX_FieldAnnotation:
        return "This field is not annotated correctly.";
      case JavaFX_Initializer:
        return "This field should be initialized by JavaFX.";
      case JavaFX_EventHandlerName:
        return "This event handler name does not match the naming convention.";
      case JavaFX_EventHandlerAnnotation:
        return "This event handler is not annotated correctly.";
      case JavaFX_EventHandlerPrivate:
        return "This event handler should have a different access modifier.";

      case MissingOverride:
        return "This method should be marked with an @Override annotation";

      case UnqualifiedStaticAccess_Method:
        return "This static method call should reference the class name";

      case UnqualifiedStaticAccess_Field:
        return "This static field access should reference the class name";

      case FinalizeOverride:
        return "Object.finalize should not be overridden.";

      default:
        throw new IllegalArgumentException("Unknown type: " + this);
    }
  }

  @Override
  public String toString() {
    return name().replaceAll("_", "").replaceAll("(.)([A-Z])", "$1 $2");
  }
}
