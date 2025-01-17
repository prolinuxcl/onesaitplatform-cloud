var svgEditorLang_hu = (function () {
  'use strict';

  var lang_hu = {
    lang: 'hu',
    dir: 'ltr',
    common: {
      ok: 'Ment',
      cancel: 'Szakítani',
      key_backspace: 'backspace',
      key_del: 'delete',
      key_down: 'down',
      key_up: 'up',
      more_opts: 'More Options',
      url: 'URL',
      width: 'Width',
      height: 'Height'
    },
    misc: {
      powered_by: 'Powered by'
    },
    ui: {
      toggle_stroke_tools: 'Show/hide more stroke tools',
      palette_info: 'Kattints ide a változások töltse szín, shift-click változtatni stroke color',
      zoom_level: 'Change nagyítási',
      panel_drag: 'Click to show hide',
      quality: 'Quality:',
      pathNodeTooltip: 'Drag node to move it. Double-click node to change segment type',
      pathCtrlPtTooltip: 'Drag control point to adjust curve properties',
      pick_stroke_paint_opacity: 'Pick a Stroke Paint and Opacity',
      pick_fill_paint_opacity: 'Pick a Fill Paint and Opacity'
    },
    properties: {
      id: 'Identify the element',
      fill_color: 'Change töltse color',
      stroke_color: 'Change stroke color',
      stroke_style: 'Change stroke kötőjel style',
      stroke_width: 'Change stroke width',
      pos_x: 'Change X coordinate',
      pos_y: 'Change Y coordinate',
      linecap_butt: 'Linecap: Butt',
      linecap_round: 'Linecap: Round',
      linecap_square: 'Linecap: Square',
      linejoin_bevel: 'Linejoin: Bevel',
      linejoin_miter: 'Linejoin: Miter',
      linejoin_round: 'Linejoin: Round',
      angle: 'Váltás forgás szög',
      blur: 'Change gaussian blur value',
      opacity: 'A kijelölt elem opacity',
      circle_cx: 'Change kör CX koordináta',
      circle_cy: 'Change kör cy koordináta',
      circle_r: 'Change kör sugara',
      ellipse_cx: 'Change ellipszis&#39;s CX koordináta',
      ellipse_cy: 'Change ellipszis&#39;s cy koordináta',
      ellipse_rx: 'Change ellipszis&#39;s x sugarú',
      ellipse_ry: 'Change ellipszis&#39;s y sugara',
      line_x1: 'A sor kezd x koordináta',
      line_x2: 'A sor vége az x koordináta',
      line_y1: 'A sor kezd y koordináta',
      line_y2: 'A sor vége az y koordináta',
      rect_height: 'Change téglalap magassága',
      rect_width: 'Change téglalap szélessége',
      corner_radius: 'Change téglalap sarok sugara',
      image_width: 'Change kép szélessége',
      image_height: 'Kép módosítása height',
      image_url: 'Change URL',
      node_x: "Change node's x coordinate",
      node_y: "Change node's y coordinate",
      seg_type: 'Change Segment type',
      straight_segments: 'Straight',
      curve_segments: 'Curve',
      text_contents: 'A szöveg tartalma',
      font_family: 'Change Betűcsalád',
      font_size: 'Change font size',
      bold: 'Félkövér szöveg',
      italic: 'Dőlt szöveg'
    },
    tools: {
      main_menu: 'Main Menu',
      bkgnd_color_opac: 'Change background color / homályosság',
      connector_no_arrow: 'No arrow',
      fitToContent: 'Fit to Content',
      fit_to_all: 'Illeszkednek az összes tartalom',
      fit_to_canvas: 'Igazítás a vászonra',
      fit_to_layer_content: 'Igazítás a réteg tartalma',
      fit_to_sel: 'Igazítás a kiválasztási',
      align_relative_to: 'Képest Igazítás ...',
      relativeTo: 'relatív hogy:',
      page: 'Page',
      largest_object: 'legnagyobb objektum',
      selected_objects: 'választott tárgyak',
      smallest_object: 'legkisebb objektum',
      new_doc: 'Új kép',
      open_doc: 'Kép megnyitása',
      export_img: 'Export',
      save_doc: 'Kép mentése más',
      import_doc: 'Import Image',
      align_to_page: 'Align Element to Page',
      align_bottom: 'Alulra igazítás',
      align_center: 'Középre igazítás',
      align_left: 'Balra igazítás',
      align_middle: 'Közép-align',
      align_right: 'Jobbra igazítás',
      align_top: 'Align Top',
      mode_select: 'Válassza ki az eszközt',
      mode_fhpath: 'Ceruza eszköz',
      mode_line: 'Line Tool',
      mode_rect: 'Rectangle Tool',
      mode_square: 'Square Tool',
      mode_fhrect: 'Free-Hand téglalap',
      mode_ellipse: 'Ellipszisszelet',
      mode_circle: 'Körbe',
      mode_fhellipse: 'Free-Hand Ellipse',
      mode_path: 'Path Tool',
      mode_text: 'Szöveg eszköz',
      mode_image: 'Image Tool',
      mode_zoom: 'Zoom Tool',
      no_embed: 'NOTE: This image cannot be embedded. It will depend on this path to be displayed',
      undo: 'Visszavon',
      redo: 'Megismétléséhez',
      tool_source: 'Szerkesztés Forrás',
      wireframe_mode: 'Wireframe Mode',
      clone: 'Clone Element(s)',
      del: 'Delete Element(s)',
      group_elements: 'Csoport elemei',
      make_link: 'Make (hyper)link',
      set_link_url: 'Set link URL (leave empty to remove)',
      to_path: 'Convert to Path',
      reorient_path: 'Reorient path',
      ungroup: 'Szétbont elemei',
      docprops: 'Dokumentum tulajdonságai',
      move_bottom: 'Mozgatás lefelé',
      move_top: 'Move to Top',
      node_clone: 'Clone Node',
      node_delete: 'Delete Node',
      node_link: 'Link Control Points',
      add_subpath: 'Add sub-path',
      openclose_path: 'Open/close sub-path',
      source_save: 'Ment',
      cut: 'Cut',
      copy: 'Copy',
      paste: 'Paste',
      paste_in_place: 'Paste in Place',
      "delete": 'Delete',
      group: 'Group',
      move_front: 'Bring to Front',
      move_up: 'Bring Forward',
      move_down: 'Send Backward',
      move_back: 'Send to Back'
    },
    layers: {
      layer: 'Layer',
      layers: 'Layers',
      del: 'Réteg törlése',
      move_down: 'Mozgatása lefelé',
      "new": 'Új réteg',
      rename: 'Réteg átnevezése',
      move_up: 'Move Layer Up',
      dupe: 'Duplicate Layer',
      merge_down: 'Merge Down',
      merge_all: 'Merge All',
      move_elems_to: 'Move elements to:',
      move_selected: 'Move selected elements to a different layer'
    },
    config: {
      image_props: 'Image Properties',
      doc_title: 'Title',
      doc_dims: 'Canvas Dimensions',
      included_images: 'Included Images',
      image_opt_embed: 'Embed data (local files)',
      image_opt_ref: 'Use file reference',
      editor_prefs: 'Editor Preferences',
      icon_size: 'Icon size',
      language: 'Language',
      background: 'Editor Background',
      editor_img_url: 'Image URL',
      editor_bg_note: 'Note: Background will not be saved with image.',
      icon_large: 'Large',
      icon_medium: 'Medium',
      icon_small: 'Small',
      icon_xlarge: 'Extra Large',
      select_predefined: 'Válassza ki előre definiált:',
      units_and_rulers: 'Units & Rulers',
      show_rulers: 'Show rulers',
      base_unit: 'Base Unit:',
      grid: 'Grid',
      snapping_onoff: 'Snapping on/off',
      snapping_stepsize: 'Snapping Step-Size:',
      grid_color: 'Grid color'
    },
    notification: {
      invalidAttrValGiven: 'Invalid value given',
      noContentToFitTo: 'No content to fit to',
      dupeLayerName: 'There is already a layer named that!',
      enterUniqueLayerName: 'Please enter a unique layer name',
      enterNewLayerName: 'Please enter the new layer name',
      layerHasThatName: 'Layer already has that name',
      QmoveElemsToLayer: "Move selected elements to layer '%s'?",
      QwantToClear: 'Do you want to clear the drawing?\nThis will also erase your undo history!',
      QwantToOpen: 'Do you want to open a new file?\nThis will also erase your undo history!',
      QerrorsRevertToSource: 'There were parsing errors in your SVG source.\nRevert back to original SVG source?',
      QignoreSourceChanges: 'Ignore changes made to SVG source?',
      featNotSupported: 'Feature not supported',
      enterNewImgURL: 'Enter the new image URL',
      defsFailOnSave: 'NOTE: Due to a bug in your browser, this image may appear wrong (missing gradients or elements). It will however appear correct once actually saved.',
      loadingImage: 'Loading image, please wait...',
      saveFromBrowser: "Select 'Save As...' in your browser (possibly via file menu or right-click context-menu) to save this image as a %s file.",
      noteTheseIssues: 'Also note the following issues: ',
      unsavedChanges: 'There are unsaved changes.',
      enterNewLinkURL: 'Enter the new hyperlink URL',
      errorLoadingSVG: 'Error: Unable to load SVG data',
      URLLoadFail: 'Unable to load from URL',
      retrieving: 'Retrieving \'%s\' ...',
      popupWindowBlocked: 'Popup window may be blocked by browser',
      exportNoBlur: 'Blurred elements will appear as un-blurred',
      exportNoforeignObject: 'foreignObject elements will not appear',
      exportNoDashArray: 'Strokes will appear filled',
      exportNoText: 'Text may not appear as expected'
    }
  };

  return lang_hu;

}());
