import React, { useState } from 'react';
import { View, Text, FlatList } from 'react-native';
import { Button } from 'react-native-elements';
import InventoryList from '../../components/inventory-list/InventoryList';

interface InventoryItem {
  id: string;
  name: string;
  quantity: number;
  status: 'In Stock' | 'Low Stock';
}

const HomeScreen: React.FC = () => {
  const [inventory, setInventory] = useState<InventoryItem[]>([
    { id: '1', name: 'NFC Scanner', quantity: 10, status: 'In Stock' }
  ]);

  return (
    <View>
      <InventoryList />
      <Button title="Add Item" onPress={() => console.log('Navigate to AddItemScreen')} />
    </View>
  );
};

export default HomeScreen;
