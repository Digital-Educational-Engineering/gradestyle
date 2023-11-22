package gradestyle.validator;

public enum Type {
  Formatting_LineLength,
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
  VariableNames_Abbreviation,
  VariableNames_TypeRegex,

  PackageNames_Regex,
  PackageNames_File,

  Commenting_Meaningful,
  Commenting_FrequencyLow,
  Commenting_FrequencyHigh,

  JavaDoc_Invalid,
  JavaDoc_MissingTag,
  JavaDoc_ExtraTag,
  JavaDoc_MissingTagDescription,
  JavaDoc_TagDescriptionIndentation,
  JavaDoc_MissingSummary,
  JavaDoc_SummaryLength,
  JavaDoc_Paragraph,
  JavaDoc_EmptyLine,
  JavaDoc_TagOrder,
  JavaDoc_SingleLine,
  JavaDoc_Asterisk,
  JavaDoc_Padding,
  JavaDoc_Position,
  JavaDoc_Missing,
  JavaDoc_MissingPeriod,

  PrivateMembers,

  Ordering_StaticField,
  Ordering_StaticMethod,
  Ordering_Field,
  Ordering_Constructor,
  Ordering_Method,
  Ordering_Overloaded,
  Ordering_Import,
  Ordering_DeclarationUsage,

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

  EarlyReturn;

  public Category getCategory() {
    return Category.valueOf(name().split("_")[0]);
  }

  public String getMessage() {
    switch (this) {
      case Formatting_LineLength:
        return "This line has too many characters.";
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

      case JavaDoc_Invalid:
        return "This JavaDoc comment format is incorrect.";
      case JavaDoc_MissingTag:
        return "This JavaDoc is missing a tag.";
      case JavaDoc_ExtraTag:
        return "This JavaDoc has an extra tag.";
      case JavaDoc_MissingTagDescription:
        return "This JavaDoc tag description is missing.";
      case JavaDoc_TagDescriptionIndentation:
        return "This JavaDoc tag description is not indented correctly.";
      case JavaDoc_MissingSummary:
        return "This JavaDoc is missing a summary.";
      case JavaDoc_SummaryLength:
        return "This JavaDoc summary is too short.";
      case JavaDoc_Paragraph:
        return "This JavaDoc paragraph is not formatted correctly.";
      case JavaDoc_EmptyLine:
        return "The JavaDoc tags are not preceded by a blank line.";
      case JavaDoc_TagOrder:
        return "The JavaDoc tags are not in the correct order.";
      case JavaDoc_SingleLine:
        return "This JavaDoc should not be on a single line.";
      case JavaDoc_Asterisk:
        return "This JavaDoc is missing a leading asterisk.";
      case JavaDoc_Padding:
        return "This JavaDoc does not have whitespace after the asterisks.";
      case JavaDoc_Position:
        return "This JavaDoc is not in the correct position.";
      case JavaDoc_Missing:
        return "The JavaDoc comment is missing.";
      case JavaDoc_MissingPeriod:
        return "This JavaDoc is missing a period at the end.";

      case PrivateMembers:
        return "This instance should have a different access modifier.";

      case Ordering_StaticField:
        return "This static field is not in the correct order.";
      case Ordering_StaticMethod:
        return "This static method is not in the correct order.";
      case Ordering_Field:
        return "This instance field is not in the correct order.";
      case Ordering_Constructor:
        return "This constructor is not in the correct order.";
      case Ordering_Method:
        return "This method is not in the correct order.";
      case Ordering_Overloaded:
        return "This overloaded method is not in the correct order.";
      case Ordering_Import:
        return "This import is not in the correct order.";
      case Ordering_DeclarationUsage:
        return "This variable declaration is too far away from its usage.";

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

      case StringConcatenation:
        return "This assignment concatenates a string inside a loop.";

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

      case EarlyReturn:
        return "This conditional could have been an early return.";

      default:
        throw new IllegalArgumentException("Unknown type: " + this);
    }
  }

  @Override
  public String toString() {
    String[] parts = name().split("_");
    String name = getCategory().toString();

    if (parts.length > 1) {
      name += " " + parts[1].replaceAll("(.)([A-Z])", "$1 $2");
    }

    return name;
  }
}
