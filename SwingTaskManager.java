/*
 StockManager.java

 Sistema de Controle de Estoque - Em Java Swing
 Contém 8 telas:
  1) Login
  2) Home
  3) Cadastro de Produtos
  4) Lista de Produtos
  5) Entrada de Estoque
  6) Saída de Estoque
  7) Fornecedores
  8) Relatórios

 Observações:
 - Dados armazenados em memória (DataStore). Pode ser estendido para persistência.
 - Navegação entre telas feita com CardLayout.
*/

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class StockManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

/* -------------------- Frame principal e navegação -------------------- */
class MainFrame extends JFrame {
    CardLayout cardLayout = new CardLayout();
    JPanel cards = new JPanel(cardLayout);

    LoginPanel loginPanel;
    HomePanel homePanel;
    ProductRegisterPanel productRegisterPanel;
    ProductListPanel productListPanel;
    StockInPanel stockInPanel;
    StockOutPanel stockOutPanel;
    SuppliersPanel suppliersPanel;
    ReportsPanel reportsPanel;

    DataStore store = new DataStore();

    public MainFrame() {
        setTitle("Stock Manager - Controle de Estoque");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        // inicializar painéis
        loginPanel = new LoginPanel(this);
        homePanel = new HomePanel(this);
        productRegisterPanel = new ProductRegisterPanel(this, store);
        productListPanel = new ProductListPanel(this, store);
        stockInPanel = new StockInPanel(this, store);
        stockOutPanel = new StockOutPanel(this, store);
        suppliersPanel = new SuppliersPanel(this, store);
        reportsPanel = new ReportsPanel(this, store);

        cards.add(loginPanel, "login");
        cards.add(homePanel, "home");
        cards.add(productRegisterPanel, "register");
        cards.add(productListPanel, "list");
        cards.add(stockInPanel, "in");
        cards.add(stockOutPanel, "out");
        cards.add(suppliersPanel, "suppliers");
        cards.add(reportsPanel, "reports");

        add(cards);
        showCard("login");
    }

    public void showCard(String name) {
        // sempre atualizar painéis que exibem dados antes de mostrar
        if (name.equals("list")) productListPanel.refresh();
        if (name.equals("in")) stockInPanel.refreshProducts();
        if (name.equals("out")) stockOutPanel.refreshProducts();
        if (name.equals("suppliers")) suppliersPanel.refresh();
        if (name.equals("reports")) reportsPanel.refresh();
        cardLayout.show(cards, name);
    }
}

/* -------------------- Data models e DataStore (em memória) -------------------- */
class DataStore {
    List<Product> products = new ArrayList<>();
    List<Supplier> suppliers = new ArrayList<>();
    List<StockMovement> movements = new ArrayList<>();

    public DataStore() {
        // dados iniciais de exemplo
        Supplier s1 = new Supplier("Fornecedor A", "contato@fa.com");
        Supplier s2 = new Supplier("Fornecedor B", "contato@fb.com");
        suppliers.add(s1); suppliers.add(s2);

        products.add(new Product("P001", "Parafuso 4mm", s1, 100));
        products.add(new Product("P002", "Porca 4mm", s1, 200));
        products.add(new Product("P003", "Placa Arduino", s2, 15));

        // movimentos exemplo
        movements.add(new StockMovement(products.get(0), 50, MovementType.IN, LocalDateTime.now().minusDays(5), s1, "Entrada inicial"));
        movements.add(new StockMovement(products.get(1), 200, MovementType.IN, LocalDateTime.now().minusDays(10), s1, "Compra inicial"));
        movements.add(new StockMovement(products.get(2), 5, MovementType.OUT, LocalDateTime.now().minusDays(1), null, "Venda"));
    }

    public Optional<Product> findProductByCode(String code) {
        return products.stream().filter(p -> p.code.equalsIgnoreCase(code)).findFirst();
    }

    public Optional<Supplier> findSupplierByName(String name) {
        return suppliers.stream().filter(s -> s.name.equalsIgnoreCase(name)).findFirst();
    }
}

enum MovementType { IN, OUT }

class Product {
    String code;
    String name;
    Supplier supplier; // fornecedor preferencial
    int quantity;

    public Product(String code, String name, Supplier supplier, int quantity) {
        this.code = code;
        this.name = name;
        this.supplier = supplier;
        this.quantity = quantity;
    }

    public String toString() {
        return code + " - " + name + " (" + quantity + ")";
    }
}

class Supplier {
    String name;
    String contact;

    public Supplier(String name, String contact) {
        this.name = name;
        this.contact = contact;
    }

    public String toString() {
        return name + " [" + contact + "]";
    }
}

class StockMovement {
    Product product;
    int amount;
    MovementType type;
    LocalDateTime datetime;
    Supplier supplier; // opcional, para entradas
    String note;

    public StockMovement(Product product, int amount, MovementType type, LocalDateTime datetime, Supplier supplier, String note) {
        this.product = product;
        this.amount = amount;
        this.type = type;
        this.datetime = datetime;
        this.supplier = supplier;
        this.note = note;
    }

    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "[" + datetime.format(fmt) + "] " + (type==MovementType.IN?"+":"-") + amount + " " + product.code + " - " + product.name + (supplier!=null?(" ("+supplier.name+")"):"") + " -- " + note;
    }
}

/* -------------------- Login Panel -------------------- */
class LoginPanel extends JPanel {
    MainFrame frame;
    JTextField userField;
    JPasswordField passField;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        JPanel box = new JPanel(new GridLayout(0,1,8,8));
        box.setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel("Stock Manager");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(title);

        box.add(new JLabel("Usuário:"));
        userField = new JTextField();
        box.add(userField);

        box.add(new JLabel("Senha:"));
        passField = new JPasswordField();
        box.add(passField);

        JButton btn = new JButton("Entrar");
        btn.addActionListener(e -> {
            // login simulado: aceitar qualquer usuário/ senha não vazia
            if (!userField.getText().trim().isEmpty() && passField.getPassword().length > 0) {
                frame.showCard("home");
            } else {
                JOptionPane.showMessageDialog(this, "Digite usuário e senha (qualquer um serve).", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        box.add(btn);

        add(box);
    }
}

/* -------------------- Home Panel -------------------- */
class HomePanel extends JPanel {
    MainFrame frame;

    public HomePanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Home - Painel Principal");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(10,10,10,10));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2,4,12,12));
        grid.setBorder(new EmptyBorder(20,20,20,20));

        addButton(grid, "Cadastro de Produtos", "register");
        addButton(grid, "Lista de Produtos", "list");
        addButton(grid, "Entrada de Estoque", "in");
        addButton(grid, "Saída de Estoque", "out");
        addButton(grid, "Fornecedores", "suppliers");
        addButton(grid, "Relatórios", "reports");
        addButton(grid, "Atualizar exibição", null);
        addButton(grid, "Sair (Logout)", "login");

        add(grid, BorderLayout.CENTER);

        JLabel footer = new JLabel("Sistema de Controle de Estoque");
        footer.setBorder(new EmptyBorder(10,10,10,10));
        add(footer, BorderLayout.SOUTH);
    }

    private void addButton(JPanel p, String text, String card) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(150,80));
        if (card != null) {
            b.addActionListener(e -> frame.showCard(card));
        } else {
            b.addActionListener(e -> JOptionPane.showMessageDialog(this, "Atualizado."));
        }
        p.add(b);
    }
}

/* -------------------- Cadastro de Produtos -------------------- */
class ProductRegisterPanel extends JPanel {
    MainFrame frame;
    DataStore store;

    public ProductRegisterPanel(MainFrame frame, DataStore store) {
        this.frame = frame; this.store = store;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Voltar"); back.addActionListener(e->frame.showCard("home"));
        top.add(back);
        top.add(new JLabel("Cadastro de Produtos"));
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(new EmptyBorder(20,40,20,40));

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<Supplier> supplierCombo = new JComboBox<>();
        JTextField qtyField = new JTextField("0");

        // carregar fornecedores atuais
        supplierCombo.addItem(null);
        for (Supplier s : store.suppliers) supplierCombo.addItem(s);

        form.add(new JLabel("Código:")); form.add(codeField);
        form.add(new JLabel("Nome:")); form.add(nameField);
        form.add(new JLabel("Fornecedor:")); form.add(supplierCombo);
        form.add(new JLabel("Quantidade inicial:")); form.add(qtyField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton save = new JButton("Salvar Produto");
        save.addActionListener(e -> {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            Supplier sup = (Supplier) supplierCombo.getSelectedItem();
            int qty = 0;
            try { qty = Integer.parseInt(qtyField.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Quantidade inválida"); return; }
            if (code.isEmpty() || name.isEmpty()) { JOptionPane.showMessageDialog(this, "Código e nome são obrigatórios"); return; }
            if (store.findProductByCode(code).isPresent()) { JOptionPane.showMessageDialog(this, "Já existe produto com esse código"); return; }
            Product p = new Product(code, name, sup, qty);
            store.products.add(p);
            // registrar movimento de entrada se qtd inicial > 0
            if (qty > 0) store.movements.add(new StockMovement(p, qty, MovementType.IN, LocalDateTime.now(), sup, "Saldo inicial"));
            JOptionPane.showMessageDialog(this, "Produto salvo com sucesso");
            // limpar campos
            codeField.setText(""); nameField.setText(""); qtyField.setText("0");
            supplierCombo.removeAllItems();
            supplierCombo.addItem(null);
            for (Supplier s : store.suppliers) supplierCombo.addItem(s);
        });
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> frame.showCard("home"));
        bottom.add(save); bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);
    }
}

/* -------------------- Lista de Produtos -------------------- */
class ProductListPanel extends JPanel {
    MainFrame frame;
    DataStore store;
    DefaultListModel<Product> listModel = new DefaultListModel<>();
    JList<Product> productList;
    JTextArea detailsArea;

    public ProductListPanel(MainFrame frame, DataStore store) {
        this.frame = frame; this.store = store;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Voltar"); back.addActionListener(e->frame.showCard("home"));
        top.add(back);
        top.add(new JLabel("Lista de Produtos"));
        add(top, BorderLayout.NORTH);

        productList = new JList<>(listModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.addListSelectionListener(e -> showDetails());
        add(new JScrollPane(productList), BorderLayout.WEST);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(0,1,6,6));
        JButton edit = new JButton("Editar (nome/fornecedor)");
        edit.addActionListener(e -> editProduct());
        JButton delete = new JButton("Remover Produto");
        delete.addActionListener(e -> deleteProduct());
        right.add(edit); right.add(delete);
        add(right, BorderLayout.EAST);

        refresh();
    }

    public void refresh() {
        listModel.clear();
        for (Product p : store.products) listModel.addElement(p);
    }

    void showDetails() {
        Product p = productList.getSelectedValue();
        if (p == null) { detailsArea.setText(""); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Código: ").append(p.code).append("\n");
        sb.append("Nome: ").append(p.name).append("\n");
        sb.append("Quantidade atual: ").append(p.quantity).append("\n");
        sb.append("Fornecedor preferencial: ").append(p.supplier != null ? p.supplier.name + " (" + p.supplier.contact + ")" : "—").append("\n\n");
        sb.append("Movimentos recentes:\n");
        store.movements.stream()
                .filter(m -> m.product == p)
                .sorted(Comparator.comparing((StockMovement m) -> m.datetime).reversed())
                .limit(10)
                .forEach(m -> sb.append(m.toString()).append("\n"));
        detailsArea.setText(sb.toString());

    }

    void editProduct() {
        Product p = productList.getSelectedValue();
        if (p == null) { JOptionPane.showMessageDialog(this, "Selecione um produto"); return; }
        JTextField nameF = new JTextField(p.name);
        JComboBox<Supplier> supCombo = new JComboBox<>();
        supCombo.addItem(null);
        for (Supplier s : store.suppliers) supCombo.addItem(s);
        supCombo.setSelectedItem(p.supplier);
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("Nome:")); panel.add(nameF);
        panel.add(new JLabel("Fornecedor:")); panel.add(supCombo);
        if (JOptionPane.showConfirmDialog(this, panel, "Editar Produto", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            p.name = nameF.getText().trim();
            p.supplier = (Supplier) supCombo.getSelectedItem();
            refresh();
            JOptionPane.showMessageDialog(this, "Produto atualizado");
        }
    }

    void deleteProduct() {
        Product p = productList.getSelectedValue();
        if (p == null) { JOptionPane.showMessageDialog(this, "Selecione um produto"); return; }
        int res = JOptionPane.showConfirmDialog(this, "Remover produto " + p.code + " ?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            // remover movimentos associados
            store.movements.removeIf(m -> m.product == p);
            store.products.remove(p);
            refresh();
            detailsArea.setText("");
            JOptionPane.showMessageDialog(this, "Produto removido");
        }
    }
}

/* -------------------- Entrada de Estoque -------------------- */
class StockInPanel extends JPanel {
    MainFrame frame;
    DataStore store;
    JComboBox<Product> productCombo;
    JComboBox<Supplier> supplierCombo;
    JTextField amountField;
    JTextField noteField;

    public StockInPanel(MainFrame frame, DataStore store) {
        this.frame = frame; this.store = store;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Voltar"); back.addActionListener(e->frame.showCard("home"));
        top.add(back);
        top.add(new JLabel("Entrada de Estoque"));
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(new EmptyBorder(20,40,20,40));

        productCombo = new JComboBox<>();
        supplierCombo = new JComboBox<>();
        amountField = new JTextField("1");
        noteField = new JTextField();

        form.add(new JLabel("Produto:")); form.add(productCombo);
        form.add(new JLabel("Fornecedor (opcional):")); form.add(supplierCombo);
        form.add(new JLabel("Quantidade:")); form.add(amountField);
        form.add(new JLabel("Observação:")); form.add(noteField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton save = new JButton("Registrar Entrada");
        save.addActionListener(e -> registerIn());
        JButton cancel = new JButton("Cancelar"); cancel.addActionListener(e->frame.showCard("home"));
        bottom.add(save); bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);

        refreshProducts();
    }

    public void refreshProducts() {
        productCombo.removeAllItems();
        for (Product p : store.products) productCombo.addItem(p);
        supplierCombo.removeAllItems();
        supplierCombo.addItem(null);
        for (Supplier s : store.suppliers) supplierCombo.addItem(s);
    }

    void registerIn() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) { JOptionPane.showMessageDialog(this, "Selecione um produto"); return; }
        Supplier s = (Supplier) supplierCombo.getSelectedItem();
        int amount;
        try { amount = Integer.parseInt(amountField.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Quantidade inválida"); return; }
        if (amount <= 0) { JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que zero"); return; }
        p.quantity += amount;
        String note = noteField.getText().trim();
        StockMovement m = new StockMovement(p, amount, MovementType.IN, LocalDateTime.now(), s, note.isEmpty() ? "Entrada manual" : note);
        store.movements.add(m);
        JOptionPane.showMessageDialog(this, "Entrada registrada. Quantidade atual: " + p.quantity);
        // limpar
        amountField.setText("1");
        noteField.setText("");
    }
}

/* -------------------- Saída de Estoque -------------------- */
class StockOutPanel extends JPanel {
    MainFrame frame;
    DataStore store;
    JComboBox<Product> productCombo;
    JTextField amountField;
    JTextField noteField;

    public StockOutPanel(MainFrame frame, DataStore store) {
        this.frame = frame; this.store = store;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Voltar"); back.addActionListener(e->frame.showCard("home"));
        top.add(back);
        top.add(new JLabel("Saída de Estoque"));
        add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(new EmptyBorder(20,40,20,40));

        productCombo = new JComboBox<>();
        amountField = new JTextField("1");
        noteField = new JTextField();

        form.add(new JLabel("Produto:")); form.add(productCombo);
        form.add(new JLabel("Quantidade:")); form.add(amountField);
        form.add(new JLabel("Observação:")); form.add(noteField);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton save = new JButton("Registrar Saída");
        save.addActionListener(e -> registerOut());
        JButton cancel = new JButton("Cancelar"); cancel.addActionListener(e->frame.showCard("home"));
        bottom.add(save); bottom.add(cancel);
        add(bottom, BorderLayout.SOUTH);

        refreshProducts();
    }

    public void refreshProducts() {
        productCombo.removeAllItems();
        for (Product p : store.products) productCombo.addItem(p);
    }

    void registerOut() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) { JOptionPane.showMessageDialog(this, "Selecione um produto"); return; }
        int amount;
        try { amount = Integer.parseInt(amountField.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Quantidade inválida"); return; }
        if (amount <= 0) { JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que zero"); return; }
        if (p.quantity < amount) { JOptionPane.showMessageDialog(this, "Estoque insuficiente (atual: " + p.quantity + ")"); return; }
        p.quantity -= amount;
        String note = noteField.getText().trim();
        StockMovement m = new StockMovement(p, amount, MovementType.OUT, LocalDateTime.now(), null, note.isEmpty() ? "Saída manual" : note);
        store.movements.add(m);
        JOptionPane.showMessageDialog(this, "Saída registrada. Quantidade atual: " + p.quantity);
        // limpar
        amountField.setText("1");
        noteField.setText("");
    }
}

/* -------------------- Fornecedores (CRUD simples) -------------------- */
class SuppliersPanel extends JPanel {
    MainFrame frame;
    DataStore store;
    DefaultListModel<Supplier> listModel = new DefaultListModel<>();
    JList<Supplier> supplierList;
    JTextField nameField, contactField;

    public SuppliersPanel(MainFrame frame, DataStore store) {
        this.frame = frame; this.store = store;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Voltar"); back.addActionListener(e->frame.showCard("home"));
        top.add(back);
        top.add(new JLabel("Fornecedores"));
        add(top, BorderLayout.NORTH);

        supplierList = new JList<>(listModel);
        supplierList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierList.addListSelectionListener(e -> showSelected());
        add(new JScrollPane(supplierList), BorderLayout.WEST);

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.setBorder(new EmptyBorder(20,40,20,40));
        nameField = new JTextField();
        contactField = new JTextField();
        form.add(new JLabel("Nome:")); form.add(nameField);
        form.add(new JLabel("Contato:")); form.add(contactField);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton addBtn = new JButton("Adicionar");
        addBtn.addActionListener(e -> addSupplier());
        JButton updateBtn = new JButton("Atualizar");
        updateBtn.addActionListener(e -> updateSupplier());
        JButton deleteBtn = new JButton("Excluir");
        deleteBtn.addActionListener(e -> deleteSupplier());
        buttons.add(addBtn); buttons.add(updateBtn); buttons.add(deleteBtn);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    void refresh() {
        listModel.clear();
        for (Supplier s : store.suppliers) listModel.addElement(s);
    }

    void showSelected() {
        Supplier s = supplierList.getSelectedValue();
        if (s == null) { nameField.setText(""); contactField.setText(""); return; }
        nameField.setText(s.name); contactField.setText(s.contact);
    }

    void addSupplier() {
        String n = nameField.getText().trim(); String c = contactField.getText().trim();
        if (n.isEmpty()) { JOptionPane.showMessageDialog(this, "Nome obrigatório"); return; }
        store.suppliers.add(new Supplier(n, c));
        refresh(); JOptionPane.showMessageDialog(this, "Fornecedor adicionado");
        nameField.setText(""); contactField.setText("");
    }

    void updateSupplier() {
        Supplier s = supplierList.getSelectedValue();
        if (s == null) { JOptionPane.showMessageDialog(this, "Selecione um fornecedor"); return; }
        s.name = nameField.getText().trim(); s.contact = contactField.getText().trim();
        refresh(); JOptionPane.showMessageDialog(this, "Fornecedor atualizado");
    }

    void deleteSupplier() {
        Supplier s = supplierList.getSelectedValue();
        if (s == null) { JOptionPane.showMessageDialog(this, "Selecione um fornecedor"); return; }
        // checar se existe produto vinculado
        boolean used = store.products.stream().anyMatch(p -> p.supplier == s);
        if (used) { JOptionPane.showMessageDialog(this, "Não é possível excluir: fornecedor vinculado a produtos"); return; }
        store.suppliers.remove(s);
        refresh(); JOptionPane.showMessageDialog(this, "Fornecedor excluído");
    }
}

/* -------------------- Relatórios -------------------- */
class ReportsPanel extends JPanel {
    MainFrame frame;
    DataStore store;
    JTextArea reportArea;

    public ReportsPanel(MainFrame frame, DataStore store) {
        this.frame = frame; this.store = store;
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("Voltar"); back.addActionListener(e->frame.showCard("home"));
        top.add(back);
        top.add(new JLabel("Relatórios"));
        add(top, BorderLayout.NORTH);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton genStock = new JButton("Gerar: Estoque Atual");
        genStock.addActionListener(e -> genStockReport());
        JButton genMov = new JButton("Gerar: Movimentos Recentes");
        genMov.addActionListener(e -> genMovementsReport());
        buttons.add(genStock); buttons.add(genMov);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        // opcional: gerar relatório automaticamente ao abrir
    }

    void genStockReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Relatório - Estoque Atual\n\n");
        for (Product p : store.products) {
            sb.append(String.format("%s | %s | Qtde: %d | Fornecedor: %s\n",
                    p.code, p.name, p.quantity, p.supplier != null ? p.supplier.name : "—"));
        }
        reportArea.setText(sb.toString());
    }

    void genMovementsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Relatório - Movimentos (últimos 50)\n\n");
        store.movements.stream()
                .sorted(Comparator.comparing(m -> m.datetime))
                .skip(Math.max(0, store.movements.size() - 50))
                .forEach(m -> sb.append(m.toString()).append("\n"));
        reportArea.setText(sb.toString());
    }
}
