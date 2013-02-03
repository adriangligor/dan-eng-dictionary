package at.gligor.dictionary;

import at.gligor.dictionary.index.DataIndexer;
import at.gligor.dictionary.index.SearchResult;
import com.google.common.collect.Multimap;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

import static at.gligor.dictionary.Lang.DAN;
import static at.gligor.dictionary.Lang.ENG;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

public class DictionaryForm {

    private JPanel rootPanel;
    private JTextField danSearchField;
    private JButton danSearchButton;
    private JEditorPane danSearchResult;
    private JTextField engSearchField;
    private JButton engSearchButton;
    private JEditorPane engSearchResult;
    private Dictionary danIndex;
    private Dictionary engIndex;

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Danish-English");
        final DictionaryForm form = new DictionaryForm();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public DictionaryForm() {
        loadIndexWorker(DAN, danSearchButton, new Callback<Dictionary>() {
            @Override
            public void apply(Dictionary input) {
                DictionaryForm.this.danIndex = input;
            }
        }).execute();
        loadIndexWorker(ENG, engSearchButton, new Callback<Dictionary>() {
            @Override
            public void apply(Dictionary input) {
                DictionaryForm.this.engIndex = input;
            }
        }).execute();

        engSearchField.addActionListener(createEngSearchActionListener());
        engSearchButton.addActionListener(createEngSearchActionListener());

        danSearchField.addActionListener(createDanSearchActionListener());
        danSearchButton.addActionListener(createDanSearchActionListener());

        final HyperlinkListener listener = createHyperlinkListener();
        engSearchResult.addHyperlinkListener(listener);
        danSearchResult.addHyperlinkListener(listener);
    }

    private ActionListener createDanSearchActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String lookUp = danSearchField.getText();
                searchWorker(danIndex, ENG, danSearchResult, lookUp).execute();
            }
        };
    }

    private ActionListener createEngSearchActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String lookUp = engSearchField.getText();
                searchWorker(engIndex, DAN, engSearchResult, lookUp).execute();
            }
        };
    }

    private SwingWorker loadIndexWorker(final Lang lang, final JButton searchButton, final Callback<Dictionary> callback) {
        return new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                final Multimap<String, SearchResult> index = new DataIndexer().loadIndex(lang);
                final Dictionary dictionary = new Dictionary(lang, index);
                callback.apply(dictionary);
                return null;
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
            }
        };
    }

    private SwingWorker<String, Void> searchWorker(final Dictionary dictionary, final Lang to, final JEditorPane searchResult, final String lookUp) {
        return new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return dictionary.translate(lookUp, to);
            }

            @Override
            protected void done() {
                try {
                    searchResult.setText(get());
                    searchResult.setCaretPosition(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static HyperlinkListener createHyperlinkListener() {
        return new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent ev) {
                try {
                    if (ev.getEventType() == ACTIVATED) {
                        final Desktop desktop = Desktop.getDesktop();
                        desktop.browse(ev.getURL().toURI());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static interface Callback<V> {
        public void apply(V value);
    }

}
