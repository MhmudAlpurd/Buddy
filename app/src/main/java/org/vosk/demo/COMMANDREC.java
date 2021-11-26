package org.vosk.demo;
import java.util.* ;
import java.util.ArrayList;

public class COMMANDREC {

    //Global vars
    static int MAIN_LST_INDX ;
    static boolean Found_MADULE = false ;
    static int Selected_Madule_index;
    static boolean Existance = false;
    static String FINAL_MADULE;
    static String FINAL_OBJECT = "Nothing";
    static int FINAL_MADULE_IDX;
    static String REPEAT_MESSAGE = "Please, Repeat Your Command!";
    static String FINAL_RESULT ;

    public static void main(String args[]) {

    }

    public static String find_madule_and_object(String RESULT_TEXT) {


        String first_word = "" ;
        String second_word = "" ;

        //Main_Keywords
        String[] text_reading_main_keywords ={"read", "written", "rich", "red", "reach", "write"};
        List<String> text_reading_main_keyworks_lst = Arrays.asList(text_reading_main_keywords);

        String[] finding_object_main_keyworkds ={"where", "what", "there", "here", "wear", "burn", "wear"};
        List<String> finding_object_main_keyworks_lst = Arrays.asList(finding_object_main_keyworkds);

        String[] scene_description_main_keywords ={"explain", "explaining", "explore", "exploring", "explode", "explicit", "expand", "express", "explainit"};
        List<String> scene_description_main_keyworks_lst = Arrays.asList(scene_description_main_keywords);


        // Secondary_Keywords!
        String[] text_reading_secondary_keywords = {"cash", "call", "label", "catch"};
        List<String> text_reading_secondary_keywords_lst = Arrays.asList(text_reading_secondary_keywords);

        String[] finding_object_secondary_keyworkds = {"is", "my"};
        List<String> finding_object_secondary_keyworkds_lst = Arrays.asList(finding_object_secondary_keyworkds);

        String[] scene_description_secondary_keywords = {"it", "me"};
        List<String> scene_description_secondary_keywords_lst = Arrays.asList(scene_description_secondary_keywords);


        //MADULES LIST
        String[] madules_names = {"Text Reading", "Finding Object", "Scene Discription"};
        List<String> madules_names_lst = Arrays.asList(madules_names);



        //List of main lists name
        List<List<String>> main_lst_names_lst = new ArrayList<>();
        main_lst_names_lst.add(text_reading_main_keyworks_lst);
        main_lst_names_lst.add(finding_object_main_keyworks_lst);
        main_lst_names_lst.add(scene_description_main_keyworks_lst);

        //List of seondary lists name
        List<List<String>> secondary_lst_names_lst = new ArrayList<>();
        secondary_lst_names_lst.add(text_reading_secondary_keywords_lst);
        secondary_lst_names_lst.add(finding_object_secondary_keyworkds_lst);
        secondary_lst_names_lst.add(scene_description_secondary_keywords_lst);



        // Split reslut!
        String result_txt = RESULT_TEXT;
        String[] result_words = result_txt.split(" ");
        ArrayList<String> result_words_lst=new ArrayList<String>();

        //preprocessing result words.
        for(String i : result_words){
            i = i.toLowerCase().trim();
            result_words_lst.add(i);
        }

        //convert list to collection to use remove() function.
        Collection result_word_lst_col= new ArrayList(result_words_lst);

        //length of result word list.
        int result_words_len = result_words_lst.size();

        // Find the Madule
        if(result_words_len >= 2){
            first_word = result_words_lst.get(0).toLowerCase().trim();

            // the  main lists include the first word?
            int idx_1 = find_madule_index(main_lst_names_lst, first_word);
            pr("idx_1: " + idx_1 );
            switch(idx_1){
                case -1:
                    //TODO: check second word!
                    second_word = result_words_lst.get(1).toLowerCase().trim();
                    int idx_2 = find_madule_index(main_lst_names_lst, second_word);
                    switch(idx_2){
                        case -1:
                            FINAL_RESULT = REPEAT_MESSAGE;
                            break;
                        default:
                            int response = double_check(result_words_lst, second_word , secondary_lst_names_lst, idx_2);
                            switch(response){
                                case -1:
                                    pr(REPEAT_MESSAGE);
                                    FINAL_RESULT = REPEAT_MESSAGE;
                                    break;
                                default:
                                    FINAL_MADULE = madules_names[FINAL_MADULE_IDX];
                                    FINAL_RESULT = FINAL_MADULE + " " + FINAL_OBJECT ;
                                    pr(FINAL_RESULT);
                                    break;
                            }
                            break;
                    }
                    break;
                default:
                    //Double check with secondary lists.
                    //just search in same category of secondary lists and main lists.
                    pr("double_check_section");
                    int response = double_check(result_words_lst, first_word , secondary_lst_names_lst, idx_1);
                    pr("response" + response);
                    pr("firstword: "+ first_word);
                    switch(response){
                        case -1:
                            pr(REPEAT_MESSAGE);
                            FINAL_RESULT = REPEAT_MESSAGE;
                            break;
                        default:
                            FINAL_MADULE = madules_names[FINAL_MADULE_IDX];
                            FINAL_RESULT = FINAL_MADULE + " " + FINAL_OBJECT ;
                            pr(FINAL_RESULT);
                            break;
                    }
                    break;
            }
        }else{
            FINAL_RESULT = REPEAT_MESSAGE;
            pr(FINAL_RESULT);
        }


        return (FINAL_RESULT);
    }

    //Global
    //pr: this method prints the input strings.
    public static void pr(String txt) {
        System.out.println(txt);
    }

    //rmv_lst: this method removes desired element from desired list.
    public static void rmv_lst(List lst, int idx) throws Exception {
        lst.remove(0);
    }

    //find_madule_index: this method recieves a list of lists and word and recognizes the word is included with which sublist.
    public static int find_madule_index(List<List<String>> lst_keywords,String word){
        MAIN_LST_INDX = -1; //because index is started from zero. and in the next steps we have MAIN_LST_INDX =+1
        Found_MADULE = false;
        Selected_Madule_index = 0;

        for(List i: lst_keywords){
            MAIN_LST_INDX += 1 ;
            if(i.contains(word)){
                Found_MADULE = true;
                Selected_Madule_index = MAIN_LST_INDX ;
            }
        }
        if (Found_MADULE == false){
            Selected_Madule_index = -1;
        }

        return (Selected_Madule_index);
    }

    //lst_contains_word: this method recognizes disired word exists in the recieved list or not.
    public static boolean lst_contains_word(List<List<String>> lst_keywords,String word, int desired_lst_idx){
        Existance = false;
        for(String i: lst_keywords.get(desired_lst_idx)){
            if(i.equals(word)){  //find word exactly!
                Existance = true;
                break;
            }
        }
        //pr("Existance: " + Existance);
        return (Existance);
    }

    //find object: if enabled madule was "finding object", this madule finds objects that mentioned in the result text.
    public static String find_object(List lst){
        int wrd_idx = -1 ;
        String userObject = "";
        for(Object i: lst){
            wrd_idx += 1 ;
            if(String.valueOf(i).equals("my") & wrd_idx != (lst.size()-1)){
                userObject = lst.get(wrd_idx + 1).toString();
                break;
            }else{
                userObject = "nothing";
            }
        }
        return (userObject);
    }

    //when the first_word verified, this method runs to check second_word.(to reduce errors)!
    public static int double_check(List list_result_word, String removed_word, List secondary_names_list, int idx_element_secondary_names){
        int not_found = 0;
        List<String> double_check_output = new ArrayList();
        list_result_word.remove(removed_word);
        pr("list result word: " + list_result_word);
        for (Object k :list_result_word ){
            boolean word_exists = lst_contains_word(secondary_names_list, k.toString(), idx_element_secondary_names );
            if(word_exists){
                FINAL_MADULE_IDX = idx_element_secondary_names;
                if (idx_element_secondary_names == 1) {
                    FINAL_OBJECT =  find_object(list_result_word);
                }
                break;
            }
        }
        if(Existance==false){
            not_found = -1;
        }
        return not_found;
    }




}
