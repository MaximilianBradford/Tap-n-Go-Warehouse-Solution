import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';

type InventoryItem = {
  id: string;
  name: string;
  quantity: number;
};

const inventoryData: InventoryItem[] = [
  { id: '1', name: 'Item 1', quantity: 10 },
  { id: '2', name: 'Item 2', quantity: 20 },
  { id: '3', name: 'Item 3', quantity: 15 },
  // Add more items as needed
];

const InventoryList: React.FC = () => {
  const renderItem = ({ item }: { item: InventoryItem }) => (
    <View style={styles.itemContainer}>
      <Text style={styles.itemText}>{item.name}</Text>
      <Text style={styles.itemText}>Quantity: {item.quantity}</Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Inventory List</Text>
      <FlatList
        data={inventoryData}
        renderItem={renderItem}
        keyExtractor={(item: InventoryItem) => item.id}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f0f0f0',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 16,
    textAlign: 'center',
  },
  itemContainer: {
    padding: 12,
    marginBottom: 8,
    backgroundColor: '#ffffff',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.5,
  },
  itemText: {
    fontSize: 18,
    color: '#333',
  },
});

export default InventoryList;
