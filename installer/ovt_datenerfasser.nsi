;NSIS Modern User Interface
;OVT Datenerfasser installer
;Written by sebastian krämer

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  Name "OVT Datenerfasser"
  OutFile "ovtdatenerfasser.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\ovt\datenerfasser"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\datenerfasser" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "German"

;--------------------------------
;Installer Sections

Section "OVT Datenerfasser" SecDummy

  SetOutPath "$INSTDIR"
  
  ;main jar file
  File ..\finaljars\OVTDatenerfasser.jar
  ;resources but exclude svn directory 
  File /r /x .svn ..\resources
  
  ;Store installation folder
  WriteRegStr HKCU "Software\datenerfasser" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  ; create desktop shortcut
  CreateShortCut "$DESKTOP\OVTDatenerfasser.lnk" "$INSTDIR\OVTDatenerfasser.jar" "" "$INSTDIR\resources\ovt-mafo.ico"
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecDummy ${LANG_ENGLISH} "Installiert den OVT Datenerfasser."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} $(DESC_SecDummy)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ;ADD YOUR OWN FILES HERE...
  Delete "$INSTDIR\*"
  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\datenerfasser"

SectionEnd