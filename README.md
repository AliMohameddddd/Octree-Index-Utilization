# Database Application Project

## Introduction
This project involves building a small database engine designed to handle simple database operations with enhanced functionality using Octree indices. The engine supports creating tables, inserting tuples, deleting tuples, and creating indices, providing a practical foundation in database management systems.

## Project Overview
The core functionalities of this database engine include:
- **Table and Data Management**: Managing data through basic CRUD operations (Create, Read, Update, Delete).
- **Indexing with Octrees**: Enhancing query performance using Octree indices for multi-dimensional data.
- **Efficient Data Storage**: Utilizing pages stored on disk to efficiently manage memory and system resources.

## Motivation
This system was developed as a part of an educational curriculum to give students hands-on experience with:
- **Data Structure Implementation**
- **Efficiency in Data Retrieval**
- **Application of Indexing Mechanisms**
- **Understanding of Low-level Data Management**

## Features

### Core Functionalities
- **Table Creation and Management**: Dynamically create and manage tables with varying data types.
- **CRUD Operations**: Support for insert, update, select, and delete operations.
- **Index Management**: Implement and utilize Octree indices for efficient query processing.

### Technical Specifications
- **Data Types Supported**: Integer, String, Double, Date.
- **Custom Storage Format**: Data is stored in serialized pages, using vectors for thread-safe operations.
- **Lazy Loading**: Pages are loaded into memory only when required, optimizing memory usage.

## Technical Framework

### Technologies Used
- **Java**: Primary programming language for building the database engine and handling operations.
- **Serialization**: Utilizing Java's serialization capabilities to manage data persistence on disk.

### System Design
- **Octree Indexing**: Implementing a three-dimensional indexing structure to enhance data retrieval for specified query dimensions.
- **Metadata Management**: Using a CSV file to manage table metadata including data types, indices, and constraints.

## Installation and Setup
To set up and use the database engine, follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/database-application-project.git
   ```
2. **Navigate to the project directory**:
   ```bash
   cd database-application-project
   ```
3. **Compile the Java application** (Ensure Java is installed):
   ```bash
   javac DBApp.java
   ```
4. **Run the application**:
   ```bash
   java DBApp
   ```

## How to Use
- **Create tables** with specific data types and constraints.
- **Perform data operations** such as insertions, deletions, and updates using the defined methods.
- **Utilize indexing** for efficient data retrieval.

## Contributing
Contributions to enhance functionalities or improve the efficiency of the existing system are welcome. For substantial changes, please open an issue first to discuss what you would like to change.

## Credits
This project is a collaborative effort led by the Computer Science department faculty at [University Name], involving several student contributors.

## License
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

This software is licensed under the MIT License - see the LICENSE.md file for details.
```

This README is designed to provide a thorough overview of the project, its purpose, the technologies used, and how to set it up and contribute. Adjust any specific details or links to fit your actual project implementation and repository location.
