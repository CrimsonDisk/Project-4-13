import javax.swing.*;
import java.awt.*;

public class GameOverHistory extends JDialog {
    private JTextField txtName;
    private JTextField txtScore;
    private DefaultListModel<String> listModel;
    private JList<String> historyList;

    public GameOverHistory(JFrame parent, int finalScore) {
        super(parent, "Lịch sử chơi game", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(null);

        // --- CỘT TRÁI (Nhập liệu) ---
        JLabel lblName = new JLabel("Player Name");
        lblName.setBounds(30, 40, 100, 25);
        add(lblName);

        txtName = new JTextField();
        txtName.setBounds(130, 40, 150, 25);
        add(txtName);

        JLabel lblScore = new JLabel("Amount (Score)");
        lblScore.setBounds(30, 80, 100, 25);
        add(lblScore);

        txtScore = new JTextField(String.valueOf(finalScore));
        txtScore.setBounds(130, 80, 150, 25);
        txtScore.setEditable(false);
        add(txtScore);

        JButton btnAdd = new JButton("Add");
        btnAdd.setBounds(130, 280, 70, 30);
        add(btnAdd);

        JButton btnRemove = new JButton("Remove");
        btnRemove.setBounds(210, 280, 85, 30);
        add(btnRemove);

        // --- CỘT PHẢI (Danh sách) ---
        JLabel lblList = new JLabel("Product List");
        lblList.setFont(new Font("Arial", Font.BOLD, 20));
        lblList.setBounds(330, 30, 200, 30);
        add(lblList);

        listModel = new DefaultListModel<>();
        historyList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBounds(330, 70, 220, 240);
        add(scrollPane);

        // Xử lý nút Add
        btnAdd.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (!name.isEmpty()) {
                listModel.addElement(listModel.size() + 1 + ". " + name + "  " + finalScore);
                txtName.setText("");
            }
        });

        // Xử lý nút Remove
        btnRemove.addActionListener(e -> {
            int selected = historyList.getSelectedIndex();
            if (selected != -1) listModel.remove(selected);
        });
    }
}